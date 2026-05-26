package com.example.polarionprocessor.model;

/**
 * Split view of module.xml around the homePageContent CDATA body.
 */
public class ModuleXmlContent {

    /** XML content before the CDATA HTML body. */
    private String prefix;

    /** Raw HTML body inside homePageContent CDATA. */
    private String htmlContent;

    /** XML content after the CDATA HTML body. */
    private String suffix;

    /** Original full XML content for reference and hashing. */
    private String fullXmlContent;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFullXmlContent() {
        return fullXmlContent;
    }

    public void setFullXmlContent(String fullXmlContent) {
        this.fullXmlContent = fullXmlContent;
    }

    /**
     * Rebuilds module.xml by replacing only the CDATA HTML body and preserving the surrounding XML verbatim.
     */
    public String rebuild(String newHtmlContent) {
        return prefix + newHtmlContent + suffix;
    }
}
