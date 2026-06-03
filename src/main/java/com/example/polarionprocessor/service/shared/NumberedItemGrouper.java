package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.model.shared.ParagraphInfo;
import com.example.polarionprocessor.util.HashUtils;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 将带数字条款号的段落锚点分组成 import item。
 */
@Service
public class NumberedItemGrouper {

    /**
     * 从带多级条款号的段落创建 item 块。
     *
     * "2. Definitions" 这种一级章节标题只作为边界：它会截断前一个 item 块，
     * 但它自身不会生成 item。
     */
    public List<ImportItemResult> group(String moduleName, String htmlContent, List<ParagraphInfo> paragraphs) {
        List<ImportItemResult> items = new ArrayList<ImportItemResult>();
        int itemSeq = 1;
        for (int i = 0; i < paragraphs.size(); i++) {
            ParagraphInfo anchor = paragraphs.get(i);
            if (!isItemAnchor(anchor)) {
                continue;
            }

            int endExclusive = findNextBoundaryIndex(paragraphs, i + 1);
            List<ParagraphInfo> blockParagraphs = paragraphs.subList(i, endExclusive);
            ImportItemResult item = buildItem(moduleName, htmlContent, itemSeq++, blockParagraphs);
            items.add(item);
        }
        return items;
    }

    private int findNextBoundaryIndex(List<ParagraphInfo> paragraphs, int startIndex) {
        for (int i = startIndex; i < paragraphs.size(); i++) {
            if (isGroupBoundary(paragraphs.get(i))) {
                return i;
            }
        }
        return paragraphs.size();
    }

    private boolean isItemAnchor(ParagraphInfo paragraph) {
        return !Boolean.TRUE.equals(paragraph.getInsideTable()) && TextUtils.hasText(paragraph.getOutlineNo());
    }

    private boolean isGroupBoundary(ParagraphInfo paragraph) {
        return Boolean.TRUE.equals(paragraph.getInsideTable())
                || TextUtils.hasText(paragraph.getOutlineNo())
                || TextUtils.hasText(paragraph.getSectionNo());
    }

    /**
     * 从一个连续段落块构建最终 import item。
     */
    private ImportItemResult buildItem(String moduleName,
                                       String htmlContent,
                                       int itemSeq,
                                       List<ParagraphInfo> blockParagraphs) {
        ParagraphInfo first = blockParagraphs.get(0);
        ParagraphInfo last = blockParagraphs.get(blockParagraphs.size() - 1);

        String description = buildDescription(blockParagraphs);
        String sourceOuterHtml = extractSourceOuterHtml(htmlContent, first, last);
        List<String> paragraphIds = collectParagraphIds(blockParagraphs);

        ImportItemResult item = new ImportItemResult();
        item.setSeq(itemSeq);
        item.setParagraphId(first.getParagraphId());
        item.setStartParagraphId(first.getParagraphId());
        item.setEndParagraphId(last.getParagraphId());
        item.setParagraphIds(paragraphIds);
        item.setParagraphCount(blockParagraphs.size());
        item.setOutlineNo(first.getOutlineNo());
        item.setSourceText(description);
        item.setDescription(description);
        item.setSourceTextHash(HashUtils.sha256(description));
        item.setSourceOuterHtml(sourceOuterHtml);
        item.setSourceOuterHtmlHash(HashUtils.sha256(sourceOuterHtml));
        item.setSourceStartIndex(first.getSourceStartIndex());
        item.setSourceEndIndex(last.getSourceEndIndex());
        item.setParagraphKey(moduleName + "#" + first.getParagraphId() + "#" + item.getSourceTextHash());
        return item;
    }

    private String buildDescription(List<ParagraphInfo> paragraphs) {
        StringBuilder builder = new StringBuilder();
        for (ParagraphInfo paragraph : paragraphs) {
            if (!TextUtils.hasText(paragraph.getSourceText())) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(paragraph.getSourceText());
        }
        return builder.toString();
    }

    private String extractSourceOuterHtml(String htmlContent, ParagraphInfo first, ParagraphInfo last) {
        if (htmlContent == null
                || first.getSourceStartIndex() == null
                || last.getSourceEndIndex() == null
                || first.getSourceStartIndex() < 0
                || last.getSourceEndIndex() > htmlContent.length()
                || first.getSourceStartIndex() >= last.getSourceEndIndex()) {
            return "";
        }
        return htmlContent.substring(first.getSourceStartIndex(), last.getSourceEndIndex());
    }

    private List<String> collectParagraphIds(List<ParagraphInfo> paragraphs) {
        List<String> paragraphIds = new ArrayList<String>();
        for (ParagraphInfo paragraph : paragraphs) {
            if (TextUtils.hasText(paragraph.getParagraphId())) {
                paragraphIds.add(paragraph.getParagraphId());
            }
        }
        return paragraphIds;
    }
}
