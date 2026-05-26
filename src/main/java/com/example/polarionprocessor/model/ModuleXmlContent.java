package com.example.polarionprocessor.model;

public class ModuleXmlContent {

    private String prefix;
    private String htmlContent;
    private String suffix;
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

    public String rebuild(String newHtmlContent) {
        return prefix + newHtmlContent + suffix;
    }
}
