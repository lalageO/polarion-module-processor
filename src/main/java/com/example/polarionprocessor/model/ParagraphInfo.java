package com.example.polarionprocessor.model;

/**
 * Raw paragraph facts collected from one original CDATA <p> node.
 */
public class ParagraphInfo {

    /** Position in the scanned <p> list. */
    private Integer seq;

    /** Original Polarion paragraph id, for example polarion_162. */
    private String paragraphId;

    /** Multi-level numeric clause number that can become an item anchor, for example 2.1. or 3.6.5. */
    private String outlineNo;

    /** Top-level section heading number, for example 2. in "2. Definitions"; used only as a grouping boundary. */
    private String sectionNo;

    /** Normalized visible paragraph text extracted by Jsoup. */
    private String sourceText;

    /** Exact original <p>...</p> fragment used for hashing and troubleshooting. */
    private String sourceOuterHtml;

    /** SHA-256 of sourceText. */
    private String sourceTextHash;

    /** SHA-256 of sourceOuterHtml. */
    private String sourceOuterHtmlHash;

    /** Start offset of sourceOuterHtml inside the original CDATA HTML string. */
    private Integer sourceStartIndex;

    /** End offset of sourceOuterHtml inside the original CDATA HTML string. */
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
