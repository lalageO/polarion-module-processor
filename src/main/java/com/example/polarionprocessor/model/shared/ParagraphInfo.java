package com.example.polarionprocessor.model.shared;

/**
 * 从一个原始 CDATA <p> 节点中提取出的段落基础信息。
 */
public class ParagraphInfo {

    /** 在扫描得到的 <p> 列表中的序号。 */
    private Integer seq;

    /** 原始 Polarion 段落 id，例如 polarion_162。 */
    private String paragraphId;

    /** 可作为 item 锚点的多级数字条款号，例如 2.1. 或 3.6.5.。 */
    private String outlineNo;

    /** 一级章节标题编号，例如 "2. Definitions" 中的 2.；只作为分组边界使用。 */
    private String sectionNo;

    /** Jsoup 提取并归一化后的可见段落文本。 */
    private String sourceText;

    /** 当前 <p> 是否位于 table 内部。表格内容不参与 Work Item 识别和替换。 */
    private Boolean insideTable;

    /** 精确的原始 <p>...</p> 片段，用于 hash 和问题排查。 */
    private String sourceOuterHtml;

    /** sourceText 的 SHA-256。 */
    private String sourceTextHash;

    /** sourceOuterHtml 的 SHA-256。 */
    private String sourceOuterHtmlHash;

    /** sourceOuterHtml 在原始 CDATA HTML 字符串中的起始偏移。 */
    private Integer sourceStartIndex;

    /** sourceOuterHtml 在原始 CDATA HTML 字符串中的结束偏移。 */
    private Integer sourceEndIndex;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(String paragraphId) {
        this.paragraphId = paragraphId;
    }

    public String getOutlineNo() {
        return outlineNo;
    }

    public void setOutlineNo(String outlineNo) {
        this.outlineNo = outlineNo;
    }

    public String getSectionNo() {
        return sectionNo;
    }

    public void setSectionNo(String sectionNo) {
        this.sectionNo = sectionNo;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public Boolean getInsideTable() {
        return insideTable;
    }

    public void setInsideTable(Boolean insideTable) {
        this.insideTable = insideTable;
    }

    public String getSourceOuterHtml() {
        return sourceOuterHtml;
    }

    public void setSourceOuterHtml(String sourceOuterHtml) {
        this.sourceOuterHtml = sourceOuterHtml;
    }

    public String getSourceTextHash() {
        return sourceTextHash;
    }

    public void setSourceTextHash(String sourceTextHash) {
        this.sourceTextHash = sourceTextHash;
    }

    public String getSourceOuterHtmlHash() {
        return sourceOuterHtmlHash;
    }

    public void setSourceOuterHtmlHash(String sourceOuterHtmlHash) {
        this.sourceOuterHtmlHash = sourceOuterHtmlHash;
    }

    public Integer getSourceStartIndex() {
        return sourceStartIndex;
    }

    public void setSourceStartIndex(Integer sourceStartIndex) {
        this.sourceStartIndex = sourceStartIndex;
    }

    public Integer getSourceEndIndex() {
        return sourceEndIndex;
    }

    public void setSourceEndIndex(Integer sourceEndIndex) {
        this.sourceEndIndex = sourceEndIndex;
    }
}
