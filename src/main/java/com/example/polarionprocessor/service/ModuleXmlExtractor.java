package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ModuleXmlContent;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ModuleXmlExtractor {

    private static final Pattern HOME_PAGE_CONTENT_FIELD = Pattern.compile(
            "(?s)<field\\s+id=[\"']homePageContent[\"'][^>]*>.*?</field>");
    private static final Pattern HOME_PAGE_CONTENT_CDATA = Pattern.compile(
            "(?s)(<field\\s+id=[\"']homePageContent[\"'][^>]*>\\s*<!\\[CDATA\\[)(.*?)(\\]\\]>\\s*</field>)");
    private static final Pattern TITLE_FIELD = Pattern.compile(
            "(?s)<field\\s+id=[\"']title[\"'][^>]*>(.*?)</field>");

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

    public String extractModuleTitle(String xmlContent) {
        Matcher matcher = TITLE_FIELD.matcher(xmlContent == null ? "" : xmlContent);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).trim();
    }
}
