package com.example.polarionprocessor.service;

import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ModuleXmlContent;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class ModuleXmlRewriter {

    public String rewrite(ModuleXmlContent moduleXmlContent, List<ImportItemResult> items) {
        StringBuilder htmlContent = new StringBuilder(moduleXmlContent.getHtmlContent());
        List<ImportItemResult> candidates = collectReplaceCandidates(items);
        Collections.sort(candidates, new Comparator<ImportItemResult>() {
            @Override
            public int compare(ImportItemResult left, ImportItemResult right) {
                return right.getSourceStartIndex().compareTo(left.getSourceStartIndex());
            }
        });

        for (ImportItemResult item : candidates) {
            if (!hasValidRange(item, htmlContent.length())) {
                markReplaceFailed(item, "Invalid source range for item replacement");
                continue;
            }
            htmlContent.replace(item.getSourceStartIndex(), item.getSourceEndIndex(), item.getReplacementHtml());
            item.setStatus(ItemStatus.REPLACED.name());
            item.setErrorCode(null);
            item.setErrorMessage(null);
        }
        return moduleXmlContent.rebuild(htmlContent.toString());
    }

    private List<ImportItemResult> collectReplaceCandidates(List<ImportItemResult> items) {
        List<ImportItemResult> candidates = new ArrayList<ImportItemResult>();
        for (ImportItemResult item : items) {
            if (!Boolean.TRUE.equals(item.getCandidate()) || !TextUtils.hasText(item.getReplacementHtml())) {
                continue;
            }
            candidates.add(item);
        }
        return candidates;
    }

    private boolean hasValidRange(ImportItemResult item, int htmlLength) {
        return item.getSourceStartIndex() != null
                && item.getSourceEndIndex() != null
                && item.getSourceStartIndex() >= 0
                && item.getSourceEndIndex() <= htmlLength
                && item.getSourceStartIndex() < item.getSourceEndIndex();
    }

    private void markReplaceFailed(ImportItemResult item, String message) {
        item.setStatus(ItemStatus.REPLACE_FAILED.name());
        item.setErrorCode("PARAGRAPH_REPLACE_FAILED");
        item.setErrorMessage(message);
    }
}
