package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.model.shared.ModuleXmlContent;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提取 homePageContent 的 CDATA 正文，同时保留周围 XML 文本。
 */
@Service
public class ModuleXmlExtractor {

    /** 用于区分“字段缺失”和“字段存在但没有 CDATA”。 */
    private static final Pattern HOME_PAGE_CONTENT_FIELD = Pattern.compile(
            "(?s)<field\\s+id=[\"']homePageContent[\"'][^>]*>.*?</field>");

    /** 将精确的 CDATA 正文捕获为 group 2，避免重新序列化 XML。 */
    private static final Pattern HOME_PAGE_CONTENT_CDATA = Pattern.compile(
            "(?s)(<field\\s+id=[\"']homePageContent[\"'][^>]*>\\s*<!\\[CDATA\\[)(.*?)(\\]\\]>\\s*</field>)");

    /** moduleName 未传时的可选 title 回退来源。 */
    private static final Pattern TITLE_FIELD = Pattern.compile(
            "(?s)<field\\s+id=[\"']title[\"'][^>]*>(.*?)</field>");

    /**
     * 将 module.xml 拆成前缀、CDATA HTML 正文和后缀。
     */
    public ModuleXmlContent extract(String xmlContent) {
        if (xmlContent == null) {
            throw new ModuleProcessException("HOME_PAGE_CONTENT_NOT_FOUND", "module.xml content is empty");
        }
        if (!HOME_PAGE_CONTENT_FIELD.matcher(xmlContent).find()) {
            throw new ModuleProcessException("HOME_PAGE_CONTENT_NOT_FOUND", "homePageContent field was not found");
        }

        Matcher matcher = HOME_PAGE_CONTENT_CDATA.matcher(xmlContent);
        if (!matcher.find()) {
            throw new ModuleProcessException("CDATA_NOT_FOUND", "homePageContent CDATA was not found");
        }

        ModuleXmlContent content = new ModuleXmlContent();
        content.setPrefix(xmlContent.substring(0, matcher.start(2)));
        content.setHtmlContent(matcher.group(2));
        content.setSuffix(xmlContent.substring(matcher.end(2)));
        content.setFullXmlContent(xmlContent);
        return content;
    }

    /**
     * 请求未提供 moduleName 时读取普通 title 字段。
     */
    public String extractModuleTitle(String xmlContent) {
        Matcher matcher = TITLE_FIELD.matcher(xmlContent == null ? "" : xmlContent);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).trim();
    }
}
