"""
Polarion HTML Document Parser for Dify Workflow
================================================
从 Polarion 导出的 HTML 格式文档中提取段落数组。

只做 HTML 标签层面的解析 — 不分析段落文本内容。
所有结构信息 (粗体、斜体、缩进层级) 均来源于 HTML 标签/属性。

Dify Code Node 接口:
    args: html_content (str), max_array_size (int, default=4800)
    returns: dict with "paragraphs" (list), "total_count" (int), "meta" (dict)
"""

import re
import html as html_module
from typing import List, Dict, Any


# ============================================================
# 1. HTML 工具函数
# ============================================================

def clean_html_text(inner_html: str) -> str:
    """
    去除 HTML 标签, 保留纯文本。
    - 移除 <sup> 脚注 (保留外层文本)
    - <img> 替换为 [IMAGE] 标记
    - <br/> 替换为空格
    - 解码 HTML entities
    """
    text = re.sub(r'<sup>.*?</sup>', '', inner_html, flags=re.DOTALL)
    text = re.sub(r'<img[^>]*>', ' [IMAGE] ', text)
    text = re.sub(r'<br\s*/?>', ' ', text)
    text = re.sub(r'<[^>]+>', '', text)
    text = html_module.unescape(text)
    text = re.sub(r'[ \t]+', ' ', text)
    return text.strip()


def has_html_tag(inner_html: str, tag: str) -> bool:
    """检查 inner HTML 中是否包含某个标签 (开或闭合)"""
    return bool(re.search(r'<\s*' + tag + r'[\s/>]', inner_html, re.IGNORECASE))


def extract_style_attrs(style_str: str) -> Dict[str, Any]:
    """从 style 字符串中提取结构化属性"""
    attrs = {}
    if not style_str:
        return attrs

    ml = re.search(r'margin-left:\s*(\d+)px', style_str)
    attrs['margin_left_px'] = int(ml.group(1)) if ml else 0

    ta = re.search(r'text-align:\s*(\w+)', style_str)
    attrs['text_align'] = ta.group(1) if ta else 'left'

    fw = re.search(r'font-weight:\s*(\w+)', style_str)
    attrs['font_weight'] = fw.group(1) if fw else 'normal'

    fs = re.search(r'font-size:\s*([\d.]+)pt', style_str)
    attrs['font_size_pt'] = float(fs.group(1)) if fs else 11.0

    return attrs


def margin_to_level(margin_left_px: int) -> int:
    """根据 margin-left 推断缩进层级"""
    if margin_left_px == 0:
        return 0
    elif margin_left_px <= 80:
        return 1
    elif margin_left_px <= 120:
        return 2
    elif margin_left_px <= 160:
        return 3
    elif margin_left_px <= 200:
        return 4
    return 5


# ============================================================
# 2. 核心解析: 从 HTML 提取段落
# ============================================================

def _extract_p_tags(content: str) -> List[Dict[str, Any]]:
    """
    提取所有 <p> 标签, 返回结构化数据。
    每个对象包含: id, text, level, margin, style_attrs, 以及 HTML 标签特征。
    """
    paragraphs: List[Dict[str, Any]] = []

    p_pattern = re.compile(r'<p\s+([^>]*)>(.*?)</p>', re.DOTALL)

    for match in p_pattern.finditer(content):
        attrs_str = match.group(1)
        inner_html = match.group(2)

        # --- 解析 id ---
        id_match = re.search(r'id="(\w+)"', attrs_str)
        p_id = id_match.group(1) if id_match else f'p_{len(paragraphs)}'

        # --- 解析 style ---
        style_match = re.search(r'style="([^"]*)"', attrs_str)
        style_attrs = extract_style_attrs(style_match.group(1) if style_match else '')

        # --- 解析 data-keep-next ---
        keep_next = 'data-keep-next="true"' in attrs_str

        # --- 检查 HTML 标签特征 (包括嵌套的 <span> 样式) ---
        # 粗体: <span style="font-weight: bold"> 或 <b>/<strong> 标签
        has_bold = (
            'font-weight: bold' in inner_html
            or 'font-weight:bold' in inner_html
            or has_html_tag(inner_html, 'b')
            or has_html_tag(inner_html, 'strong')
        )
        # 斜体: <span style="font-style: italic"> 或 <i>/<em> 标签
        has_italic = (
            'font-style: italic' in inner_html
            or 'font-style:italic' in inner_html
            or has_html_tag(inner_html, 'i')
            or has_html_tag(inner_html, 'em')
        )
        # 图片
        has_image = has_html_tag(inner_html, 'img')

        # 从嵌套 <span> 中提取最大的 font-size (覆盖 <p> 级别的 font_size)
        span_sizes = re.findall(r'font-size:\s*([\d.]+)pt', inner_html)
        if span_sizes:
            max_font = max(float(s) for s in span_sizes)
            style_attrs['font_size_pt'] = max_font
            if max_font > style_attrs.get('font_size_pt', 11.0):
                # 大字号通常表示标题
                pass

        # --- 清洗纯文本 ---
        plain_text = clean_html_text(inner_html)
        if not plain_text:
            continue

        paragraphs.append({
            'id': p_id,
            'text': plain_text,
            'level': margin_to_level(style_attrs.get('margin_left_px', 0)),
            'margin_left_px': style_attrs.get('margin_left_px', 0),
            'text_align': style_attrs.get('text_align', 'left'),
            'font_weight': style_attrs.get('font_weight', 'normal'),
            'font_size_pt': style_attrs.get('font_size_pt', 11.0),
            'has_bold': has_bold,
            'has_italic': has_italic,
            'has_image': has_image,
            'keep_next': keep_next,
            'has_real_id': bool(id_match),  # 标记是否拥有 HTML 原始 id
        })

    return paragraphs


def _extract_li_tags(content: str) -> List[Dict[str, Any]]:
    """提取 <li> 列表项作为补充段落"""
    items = []
    li_pattern = re.compile(r'<li[^>]*>(.*?)</li>', re.DOTALL)
    idx = 0

    for match in li_pattern.finditer(content):
        plain_text = clean_html_text(match.group(1))
        if plain_text:
            items.append({
                'id': f'li_{idx}',
                'text': plain_text,
                'level': 3,
                'margin_left_px': 0,
                'text_align': 'left',
                'font_weight': 'normal',
                'font_size_pt': 11.0,
                'has_bold': False,
                'has_italic': False,
                'has_image': False,
                'keep_next': False,
                'source_tag': 'li',
            })
            idx += 1

    return items


def _merge_no_id(paragraphs: List[Dict[str, Any]]) -> int:
    """
    将没有真实 id 的续行段落合并到前一个有 id 的段落上。
    Polarion 导出的 HTML 中, 一个多行段落可能被拆为多个 <p> 标签,
    只有第一个带有 id="polarion_xxx", 续行没有 id。

    返回被合并的段落数量。
    """
    merged_count = 0
    i = 1
    while i < len(paragraphs):
        p = paragraphs[i]
        if not p.get('has_real_id'):
            # 合并到前一个段落
            prev = paragraphs[i - 1]
            prev['text'] += ' ' + p['text']
            # 如果续行有 keep_next, 保留到父段落上
            if p.get('keep_next') and not prev.get('keep_next'):
                prev['keep_next'] = True
            del paragraphs[i]
            merged_count += 1
        else:
            i += 1
    return merged_count


def _cleanup_has_real_id(paragraphs: List[Dict[str, Any]]):
    """移除 has_real_id 临时标记"""
    for p in paragraphs:
        p.pop('has_real_id', None)
    for p in paragraphs:
        if p.get('children'):
            for c in p['children']:
                c.pop('has_real_id', None)


def _merge_keep_next(paragraphs: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    """
    处理 keep_next: 将 keep_next=true 的段落与下一条配对成组。
    每条 keep_next 段落精确合并其后 1 条, 不会连锁吞并。
    """
    grouped: List[Dict[str, Any]] = []
    i = 0

    while i < len(paragraphs):
        p = paragraphs[i]
        if p.get('keep_next') and i + 1 < len(paragraphs):
            # 将当前段落与下一条打包为一组
            child = paragraphs[i + 1]
            p['children'] = [child]
            grouped.append(p)
            i += 2  # 跳过已被合并的下一条
        else:
            grouped.append(p)
            i += 1

    return grouped


# ============================================================
# 3. Dify 入口函数
# ============================================================

def parse_html_document(html_content: str) -> List[Dict[str, Any]]:
    """
    从 HTML 中提取结构化段落数组。
    只做标签层面的解析, 不做文本内容分析。
    """
    # 剥离 CDATA 包装
    cdata_match = re.search(r'<!\[CDATA\[(.*?)\]\]>', html_content, re.DOTALL)
    content = cdata_match.group(1) if cdata_match else html_content

    # 提取 <p> 段落
    paragraphs = _extract_p_tags(content)

    # 补充 <li> 列表项
    li_items = _extract_li_tags(content)
    paragraphs.extend(li_items)

    # 将无 id 的续行段落合并到前一个有 id 的段落上
    no_id_merged = _merge_no_id(paragraphs)

    # 处理 keep_next 分组
    grouped = _merge_keep_next(paragraphs)

    # 清理临时标记
    _cleanup_has_real_id(grouped)

    return grouped, no_id_merged


def main(
    html_content: str,
    max_array_size: int = 4800,
) -> Dict[str, Any]:
    """
    Dify Code Node 入口函数。

    Args:
        html_content   : Polarion 导出的完整 HTML/XML 内容 (含 CDATA 包装)
        max_array_size : 输出数组最大长度 (默认 4800, 留余量应对 Dify 5000 上限)

    Returns:
        {
            "paragraphs": [ ... ],       # 结构化段落数组
            "total_count": int,           # 最终输出段落数
            "exceeds_limit": bool,        # 是否超出 max_array_size
            "meta": {                     # 统计信息
                "p_tags": int,            # <p> 标签总数 (清洗前)
                "li_tags": int,           # <li> 标签总数
                "empty_removed": int,     # 因空文本被移除的数量
                "no_id_merged": int,      # 无 id 续行段落合并数
                "keep_next_groups": int,  # keep_next 分组数
            }
        }
    """
    # 剥离 CDATA
    cdata_match = re.search(r'<!\[CDATA\[(.*?)\]\]>', html_content, re.DOTALL)
    content = cdata_match.group(1) if cdata_match else html_content

    # 统计原始标签数
    raw_p_count = len(re.findall(r'<p\s', content, re.DOTALL))
    raw_li_count = len(re.findall(r'<li\b', content, re.DOTALL))

    # 解析
    paragraphs, no_id_merged = parse_html_document(html_content)
    empty_removed = raw_p_count + raw_li_count - len(paragraphs) - sum(
        1 for p in paragraphs if p.get('children')
    ) - no_id_merged
    keep_next_groups = sum(1 for p in paragraphs if p.get('children'))

    exceeds_limit = len(paragraphs) > max_array_size
    if exceeds_limit:
        paragraphs = paragraphs[:max_array_size]

    return {
        'paragraphs': paragraphs,
        'total_count': len(paragraphs),
        'exceeds_limit': exceeds_limit,
        'meta': {
            'p_tags': raw_p_count,
            'li_tags': raw_li_count,
            'empty_removed': max(0, empty_removed),
            'keep_next_groups': keep_next_groups,
        },
    }



