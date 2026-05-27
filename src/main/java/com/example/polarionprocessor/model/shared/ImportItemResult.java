package com.example.polarionprocessor.model.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * 段落分组和候选筛选之后得到的一个可导入 Work Item 记录。
 */
public class ImportItemResult {

    /** 在生成 item 列表中的序号。 */
    private Integer seq;

    /** 锚点段落 id，也就是分组块中的第一个段落。 */
    private String paragraphId;

    /** 当前 item 块包含的第一个段落 id。 */
    private String startParagraphId;

    /** 当前 item 块包含的最后一个段落 id。 */
    private String endParagraphId;

    /** 当前 item 块包含的全部段落 id，用于预览和排查。 */
    private List<String> paragraphIds = new ArrayList<String>();

    /** 当前 item 块包含的原始 <p> 节点数量。 */
    private Integer paragraphCount;

    /** 锚点段落的数字条款号，例如 2.2.。 */
    private String outlineNo;

    /** 本地稳定键，由 moduleName、锚点段落 id 和文本 hash 组成。 */
    private String paragraphKey;

    /** 合并后的完整描述文本，用于标题生成以及后续 Work Item 创建。 */
    private String sourceText;

    /** sourceText 的别名，保留给后续 Polarion Work Item 的 description 字段。 */
    private String description;

    /** sourceText 的 SHA-256。 */
    private String sourceTextHash;

    /** 覆盖当前 item 块全部段落的原始 HTML 片段。 */
    private String sourceOuterHtml;

    /** sourceOuterHtml 的 SHA-256。 */
    private String sourceOuterHtmlHash;

    /** sourceOuterHtml 在原始 CDATA HTML 字符串中的起始偏移。 */
    private Integer sourceStartIndex;

    /** sourceOuterHtml 在原始 CDATA HTML 字符串中的结束偏移。 */
    private Integer sourceEndIndex;

    /** 当前分组 item 是否需要替换或导入。 */
    private Boolean candidate;

    /** candidate=false 时的机器可读跳过原因。 */
    private String skipReason;

    /** 配置的标题生成器给出的标题。 */
    private String generatedTitle;

    /** 创建 Work Item 时使用的标题；当前与 generatedTitle 相同。 */
    private String finalTitle;

    /** mock 或真实 Polarion Work Item id，例如 MOCK-000001 或 FDP-7016。 */
    private String workItemId;

    /** mock 模式下用于替换 sourceOuterHtml 的 HTML 片段。 */
    private String replacementHtml;

    /** 当前 item 的处理状态。 */
    private String status;

    /** 当前 item 的机器可读错误码。 */
    private String errorCode;

    /** 当前 item 的人工可读错误信息。 */
    private String errorMessage;

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

    public String getStartParagraphId() {
        return startParagraphId;
    }

    public void setStartParagraphId(String startParagraphId) {
        this.startParagraphId = startParagraphId;
    }

    public String getEndParagraphId() {
        return endParagraphId;
    }

    public void setEndParagraphId(String endParagraphId) {
        this.endParagraphId = endParagraphId;
    }

    public List<String> getParagraphIds() {
        return paragraphIds;
    }

    public void setParagraphIds(List<String> paragraphIds) {
        this.paragraphIds = paragraphIds;
    }

    public Integer getParagraphCount() {
        return paragraphCount;
    }

    public void setParagraphCount(Integer paragraphCount) {
        this.paragraphCount = paragraphCount;
    }

    public String getOutlineNo() {
        return outlineNo;
    }

    public void setOutlineNo(String outlineNo) {
        this.outlineNo = outlineNo;
    }

    public String getParagraphKey() {
        return paragraphKey;
    }

    public void setParagraphKey(String paragraphKey) {
        this.paragraphKey = paragraphKey;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceTextHash() {
        return sourceTextHash;
    }

    public void setSourceTextHash(String sourceTextHash) {
        this.sourceTextHash = sourceTextHash;
    }

    public String getSourceOuterHtml() {
        return sourceOuterHtml;
    }

    public void setSourceOuterHtml(String sourceOuterHtml) {
        this.sourceOuterHtml = sourceOuterHtml;
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

    public Boolean getCandidate() {
        return candidate;
    }

    public void setCandidate(Boolean candidate) {
        this.candidate = candidate;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public String getGeneratedTitle() {
        return generatedTitle;
    }

    public void setGeneratedTitle(String generatedTitle) {
        this.generatedTitle = generatedTitle;
    }

    public String getFinalTitle() {
        return finalTitle;
    }

    public void setFinalTitle(String finalTitle) {
        this.finalTitle = finalTitle;
    }

    public String getWorkItemId() {
        return workItemId;
    }

    public void setWorkItemId(String workItemId) {
        this.workItemId = workItemId;
    }

    public String getReplacementHtml() {
        return replacementHtml;
    }

    public void setReplacementHtml(String replacementHtml) {
        this.replacementHtml = replacementHtml;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
