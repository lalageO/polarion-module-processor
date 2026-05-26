package com.example.polarionprocessor.service;

import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ModuleXmlContent;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ModuleXmlRewriter {

    public String rewrite(ModuleXmlContent moduleXmlContent, List<ImportItemResult> items) {
        String htmlContent = moduleXmlContent.getHtmlContent();
        for (ImportItemResult item : items) {
            if (!Boolean.TRUE.equals(item.getCandidate()) || !TextUtils.hasText(item.getReplacementHtml())) {
                continue;
            }
            Pattern pattern = Pattern.compile(
                    "(?s)<p\\b(?=[^>]*\\bid=[\"']" + Pattern.quote(item.getParagraphId()) + "[\"'])[^>]*>.*?</p>");
            Matcher matcher = pattern.matcher(htmlContent);
            if (!matcher.find()) {
                item.setStatus(ItemStatus.REPLACE_FAILED.name());
                item.setErrorCode("PARAGRAPH_REPLACE_FAILED");
                item.setErrorMessage("Original paragraph was not found by paragraphId");
                continue;
            }
            htmlContent = matcher.replaceFirst(Matcher.quoteReplacement(item.getReplacementHtml()));
            item.setStatus(ItemStatus.REPLACED.name());
            item.setErrorCode(null);
            item.setErrorMessage(null);
        }
        return moduleXmlContent.rebuild(htmlContent);
    }
}
