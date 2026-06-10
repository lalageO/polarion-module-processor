package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.enums.PolarionItemRole;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import com.example.polarionprocessor.model.shared.ParagraphInfo;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正式导入链路的文档结构识别器，负责把数字条款分类为 heading 或 requirement。
 */
@Service
public class PolarionDocumentItemBuilder {

    public static final String TYPE_HEADING = "heading";
    public static final String TYPE_REQUIREMENT = "stakeholderrequirement";

    private static final String REASON_HEADING = "TITLE_LIKE_WITH_CHILD_OUTLINE";
    private static final String REASON_NO_CHILD = "NO_CHILD_OUTLINE";
    private static final String REASON_NOT_TITLE = "NOT_TITLE_LIKE";
    private static final int SHORT_ENGLISH_WORD_LIMIT = 12;
    private static final int SHORT_CJK_CHAR_LIMIT = 20;
    private static final int VISUAL_HEADING_TEXT_MAX_LENGTH = 120;
    private static final Pattern ENGLISH_WORD_PATTERN = Pattern.compile("[A-Za-z0-9]+(?:[-'][A-Za-z0-9]+)*");
    private static final Pattern CJK_PATTERN = Pattern.compile("[\\u4E00-\\u9FFF]");
    private static final Pattern TOC_TOP_LEVEL_WITH_PAGE_PATTERN = Pattern.compile("^\\d+\\.\\s+.+\\s+\\d+$");
    private static final Pattern TOC_INTRODUCTION_WITH_PAGE_PATTERN = Pattern.compile("(?i)^Introduction\\s+\\d+$");
    private static final Pattern TOC_APPENDIX_WITH_PAGE_PATTERN = Pattern.compile("(?i)^Appendix\\s+\\d+\\b.+\\s+\\d+$");
    private static final Pattern VISUAL_HEADING_BOLD_PATTERN =
            Pattern.compile("(?is)(font-weight\\s*:\\s*(bold|[6-9]00)|<\\s*(b|strong)\\b)");
    private static final Pattern VISUAL_HEADING_FONT_SIZE_PATTERN =
            Pattern.compile("(?is)font-size\\s*:\\s*((1[3-9]|[2-9]\\d)(\\.\\d+)?\\s*pt|(1[6-9]|[2-9]\\d)(\\.\\d+)?\\s*px)");

    public List<PolarionImportItemResult> build(String moduleName,
                                                String htmlContent,
                                                List<ParagraphInfo> paragraphs) {
        List<ParagraphInfo> anchors = collectNumericAnchors(paragraphs);
        List<PolarionImportItemResult> items = new ArrayList<PolarionImportItemResult>();
        for (int i = 0; i < anchors.size(); i++) {
            ParagraphInfo anchor = anchors.get(i);
            String outlineNo = outlineNo(anchor);
            boolean hasChildOutline = hasChildOutline(anchors, i, outlineNo);
            boolean titleLike = isTitleLike(anchor.getSourceText(), outlineNo);

            PolarionImportItemResult item = new PolarionImportItemResult();
            item.setSeq(items.size() + 1);
            item.setItemKey(anchor.getParagraphId());
            item.setStartParagraphId(anchor.getParagraphId());
            item.setOutlineNo(outlineNo);
            item.setOutlineDepth(TextUtils.outlineDepth(outlineNo));
            item.setTitleText(TextUtils.removeLeadingOutlineNo(anchor.getSourceText(), outlineNo));
            item.setHasChildOutline(hasChildOutline);
            item.setSourceStartIndex(anchor.getSourceStartIndex());

            if (hasChildOutline && titleLike) {
                applyHeading(item, anchor);
            } else {
                applyRequirement(moduleName, htmlContent, paragraphs, anchors, i, item, anchor, hasChildOutline);
            }
            items.add(item);
        }
        assignParentHeadings(items);
        return items;
    }

    private List<ParagraphInfo> collectNumericAnchors(List<ParagraphInfo> paragraphs) {
        List<ParagraphInfo> anchors = new ArrayList<ParagraphInfo>();
        if (paragraphs == null) {
            return anchors;
        }
        boolean insideTableOfContents = false;
        for (ParagraphInfo paragraph : paragraphs) {
            if (paragraph == null || Boolean.TRUE.equals(paragraph.getInsideTable())) {
                continue;
            }
            String text = TextUtils.normalizeSpaces(paragraph.getSourceText());
            if (isContentsHeading(text)) {
                insideTableOfContents = true;
                continue;
            }
            if (insideTableOfContents) {
                if (!TextUtils.hasText(text) || isTableOfContentsLine(text)) {
                    continue;
                }
                insideTableOfContents = false;
            }
            if (TextUtils.hasText(outlineNo(paragraph))) {
                anchors.add(paragraph);
            }
        }
        return anchors;
    }

    private boolean isContentsHeading(String text) {
        return "Contents".equalsIgnoreCase(TextUtils.normalizeSpaces(text))
                || "Table of Contents".equalsIgnoreCase(TextUtils.normalizeSpaces(text));
    }

    private boolean isTableOfContentsLine(String text) {
        String normalized = TextUtils.normalizeSpaces(text);
        if (!TextUtils.hasText(normalized)) {
            return true;
        }
        if ("Page".equalsIgnoreCase(normalized) || "Annexes".equalsIgnoreCase(normalized)) {
            return true;
        }
        return TOC_TOP_LEVEL_WITH_PAGE_PATTERN.matcher(normalized).matches()
                || TOC_INTRODUCTION_WITH_PAGE_PATTERN.matcher(normalized).matches()
                || TOC_APPENDIX_WITH_PAGE_PATTERN.matcher(normalized).matches();
    }

    private void applyHeading(PolarionImportItemResult item, ParagraphInfo anchor) {
        item.setEndParagraphId(anchor.getParagraphId());
        item.setDescription(anchor.getSourceText());
        item.setItemRole(PolarionItemRole.HEADING.name());
        item.setWorkItemType(TYPE_HEADING);
        item.setCandidate(Boolean.TRUE);
        item.setSkipReason(null);
        item.setDecisionReason(REASON_HEADING);
        item.setRuleTitle(buildHeadingTitle(item));
        item.setTitle(item.getRuleTitle());
        item.setStatus(ItemStatus.READY.name());
        item.setSourceEndIndex(anchor.getSourceEndIndex());
    }

    private void applyRequirement(String moduleName,
                                  String htmlContent,
                                  List<ParagraphInfo> paragraphs,
                                  List<ParagraphInfo> anchors,
                                  int anchorIndex,
                                  PolarionImportItemResult item,
                                  ParagraphInfo anchor,
                                  boolean hasChildOutline) {
        int endExclusive = findNextBoundaryIndex(paragraphs, anchor, anchors, anchorIndex);
        int anchorParagraphIndex = findParagraphIndex(paragraphs, anchor);
        List<ParagraphInfo> blockParagraphs = anchorParagraphIndex < 0
                ? singleton(anchor)
                : paragraphs.subList(anchorParagraphIndex, endExclusive);
        ParagraphInfo last = lastNonTableParagraph(blockParagraphs, anchor);

        item.setEndParagraphId(last.getParagraphId());
        item.setDescription(buildDescription(blockParagraphs));
        item.setItemRole(PolarionItemRole.REQUIREMENT.name());
        item.setWorkItemType(TYPE_REQUIREMENT);
        item.setCandidate(Boolean.TRUE);
        item.setSkipReason(null);
        item.setDecisionReason(hasChildOutline ? REASON_NOT_TITLE : REASON_NO_CHILD);
        item.setStatus(ItemStatus.READY.name());
        item.setSourceEndIndex(last.getSourceEndIndex());
        if (!TextUtils.hasText(item.getDescription())) {
            item.setDescription(anchor.getSourceText());
        }
        item.setItemKey(moduleName + "#" + anchor.getParagraphId());
        if (htmlContent == null
                || item.getSourceStartIndex() == null
                || item.getSourceEndIndex() == null
                || item.getSourceStartIndex() < 0
                || item.getSourceEndIndex() > htmlContent.length()
                || item.getSourceStartIndex() >= item.getSourceEndIndex()) {
            item.setSourceStartIndex(anchor.getSourceStartIndex());
            item.setSourceEndIndex(anchor.getSourceEndIndex());
            item.setEndParagraphId(anchor.getParagraphId());
        }
    }

    private void assignParentHeadings(List<PolarionImportItemResult> items) {
        for (PolarionImportItemResult item : items) {
            PolarionImportItemResult parent = findNearestParentHeading(items, item);
            if (parent != null) {
                item.setParentOutlineNo(parent.getOutlineNo());
            }
        }
    }

    private PolarionImportItemResult findNearestParentHeading(List<PolarionImportItemResult> items,
                                                              PolarionImportItemResult item) {
        if (!TextUtils.hasText(item.getOutlineNo())) {
            return null;
        }
        String current = normalizeOutline(item.getOutlineNo());
        PolarionImportItemResult best = null;
        for (PolarionImportItemResult candidate : items) {
            if (candidate == item) {
                break;
            }
            if (!PolarionItemRole.HEADING.name().equals(candidate.getItemRole())
                    || !TextUtils.hasText(candidate.getOutlineNo())) {
                continue;
            }
            String parent = normalizeOutline(candidate.getOutlineNo());
            if (current.startsWith(parent + ".")) {
                if (best == null || TextUtils.outlineDepth(candidate.getOutlineNo()) > TextUtils.outlineDepth(best.getOutlineNo())) {
                    best = candidate;
                }
            }
        }
        return best;
    }

    private int findNextBoundaryIndex(List<ParagraphInfo> paragraphs,
                                      ParagraphInfo anchor,
                                      List<ParagraphInfo> anchors,
                                      int anchorIndex) {
        int anchorParagraphIndex = findParagraphIndex(paragraphs, anchor);
        if (anchorParagraphIndex < 0) {
            return paragraphs == null ? 0 : paragraphs.size();
        }
        ParagraphInfo nextAnchor = anchorIndex + 1 < anchors.size() ? anchors.get(anchorIndex + 1) : null;
        int nextAnchorIndex = nextAnchor == null ? -1 : findParagraphIndex(paragraphs, nextAnchor);
        int end = nextAnchorIndex < 0 ? paragraphs.size() : nextAnchorIndex;
        for (int i = anchorParagraphIndex + 1; i < end; i++) {
            ParagraphInfo paragraph = paragraphs.get(i);
            if (paragraph == null) {
                continue;
            }
            if (Boolean.TRUE.equals(paragraph.getInsideTable()) || isVisualHeadingBoundary(paragraph)) {
                return i;
            }
        }
        return end;
    }

    private boolean isVisualHeadingBoundary(ParagraphInfo paragraph) {
        if (paragraph == null
                || TextUtils.hasText(outlineNo(paragraph))
                || !TextUtils.hasText(paragraph.getSourceText())) {
            return false;
        }
        String text = TextUtils.normalizeSpaces(paragraph.getSourceText());
        if (text.length() > VISUAL_HEADING_TEXT_MAX_LENGTH) {
            return false;
        }
        String html = paragraph.getSourceOuterHtml();
        if (!TextUtils.hasText(html)) {
            return false;
        }
        return VISUAL_HEADING_BOLD_PATTERN.matcher(html).find()
                && VISUAL_HEADING_FONT_SIZE_PATTERN.matcher(html).find();
    }

    private int findParagraphIndex(List<ParagraphInfo> paragraphs, ParagraphInfo target) {
        if (paragraphs == null || target == null) {
            return -1;
        }
        for (int i = 0; i < paragraphs.size(); i++) {
            if (paragraphs.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private ParagraphInfo lastNonTableParagraph(List<ParagraphInfo> paragraphs, ParagraphInfo fallback) {
        if (paragraphs == null || paragraphs.isEmpty()) {
            return fallback;
        }
        for (int i = paragraphs.size() - 1; i >= 0; i--) {
            ParagraphInfo paragraph = paragraphs.get(i);
            if (paragraph != null && !Boolean.TRUE.equals(paragraph.getInsideTable())) {
                return paragraph;
            }
        }
        return fallback;
    }

    private List<ParagraphInfo> singleton(ParagraphInfo paragraph) {
        List<ParagraphInfo> list = new ArrayList<ParagraphInfo>();
        list.add(paragraph);
        return list;
    }

    private String buildDescription(List<ParagraphInfo> paragraphs) {
        StringBuilder builder = new StringBuilder();
        if (paragraphs == null) {
            return "";
        }
        for (ParagraphInfo paragraph : paragraphs) {
            if (paragraph == null || Boolean.TRUE.equals(paragraph.getInsideTable())
                    || !TextUtils.hasText(paragraph.getSourceText())) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(paragraph.getSourceText());
        }
        return builder.toString();
    }

    private boolean hasChildOutline(List<ParagraphInfo> anchors, int anchorIndex, String outlineNo) {
        if (!TextUtils.hasText(outlineNo)) {
            return false;
        }
        String current = normalizeOutline(outlineNo);
        int currentDepth = TextUtils.outlineDepth(outlineNo);
        for (int i = anchorIndex + 1; i < anchors.size(); i++) {
            String nextOutlineNo = outlineNo(anchors.get(i));
            if (!TextUtils.hasText(nextOutlineNo)) {
                continue;
            }
            String next = normalizeOutline(nextOutlineNo);
            int nextDepth = TextUtils.outlineDepth(nextOutlineNo);
            if (next.startsWith(current + ".") && nextDepth > currentDepth) {
                return true;
            }
            if (nextDepth <= currentDepth) {
                return false;
            }
        }
        return false;
    }

    private boolean isTitleLike(String sourceText, String outlineNo) {
        String titleText = TextUtils.removeLeadingOutlineNo(sourceText, outlineNo);
        if (!TextUtils.hasText(titleText)) {
            return false;
        }
        boolean shortTitle = isShortTitle(titleText);
        if (endsWithColon(titleText)) {
            return shortTitle;
        }
        if (hasTerminalSentencePunctuation(titleText)) {
            return shortTitle;
        }
        return shortTitle;
    }

    private boolean isShortTitle(String titleText) {
        String normalized = TextUtils.normalizeSpaces(titleText);
        if (!TextUtils.hasText(normalized)) {
            return false;
        }
        int cjkCount = countCjk(normalized);
        if (cjkCount > 0 && cjkCount <= SHORT_CJK_CHAR_LIMIT) {
            return true;
        }
        int words = countEnglishWords(normalized);
        if (words > 0) {
            return words <= SHORT_ENGLISH_WORD_LIMIT;
        }
        return normalized.length() <= SHORT_CJK_CHAR_LIMIT;
    }

    private int countEnglishWords(String value) {
        Matcher matcher = ENGLISH_WORD_PATTERN.matcher(value == null ? "" : value);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private int countCjk(String value) {
        Matcher matcher = CJK_PATTERN.matcher(value == null ? "" : value);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private boolean endsWithColon(String value) {
        String normalized = stripTrailingClosers(TextUtils.normalizeSpaces(value));
        return normalized.endsWith(":") || normalized.endsWith("：");
    }

    private boolean hasTerminalSentencePunctuation(String value) {
        String normalized = stripTrailingClosers(TextUtils.normalizeSpaces(value));
        return normalized.endsWith(".") || normalized.endsWith("。") || normalized.endsWith("．");
    }

    private String stripTrailingClosers(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        while (normalized.endsWith(")") || normalized.endsWith("）")
                || normalized.endsWith("]") || normalized.endsWith("】")
                || normalized.endsWith("\"") || normalized.endsWith("'")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return normalized;
    }

    private String buildHeadingTitle(PolarionImportItemResult item) {
        return TextUtils.normalizeSpaces(item.getOutlineNo() + " " + item.getTitleText());
    }

    private String outlineNo(ParagraphInfo paragraph) {
        if (paragraph == null) {
            return null;
        }
        return firstText(paragraph.getOutlineNo(), paragraph.getSectionNo());
    }

    private String normalizeOutline(String outlineNo) {
        String normalized = outlineNo == null ? "" : outlineNo.trim();
        while (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (TextUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }
}
