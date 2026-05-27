package com.example.polarionprocessor.model.shared;

/**
 * 以 homePageContent 的 CDATA 内容为中心拆分出的 module.xml 视图。
 */
public class ModuleXmlContent {

    /** CDATA HTML 正文之前的 XML 内容。 */
    private String prefix;

    /** homePageContent CDATA 内部的原始 HTML 正文。 */
    private String htmlContent;

    /** CDATA HTML 正文之后的 XML 内容。 */
    private String suffix;

    /** 原始完整 XML 内容，用于参考和 hash。 */
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
     * 只替换 CDATA HTML 正文，并原样保留周围 XML 内容来重建 module.xml。
     */
    public String rebuild(String newHtmlContent) {
        return prefix + newHtmlContent + suffix;
    }
}
