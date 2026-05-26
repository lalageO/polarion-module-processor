package com.example.polarionprocessor.model;

public class ParagraphInfo {

    private Integer seq;
    private String paragraphId;
    private String outlineNo;
    private String sourceText;
    private String sourceOuterHtml;
    private String sourceTextHash;
    private String sourceOuterHtmlHash;
    private Integer sourceStartIndex;
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
