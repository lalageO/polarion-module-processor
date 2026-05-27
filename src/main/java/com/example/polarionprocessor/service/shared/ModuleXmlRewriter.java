package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.model.shared.ModuleXmlContent;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 在原始 CDATA HTML 正文中替换候选 item 对应的 HTML 范围。
 */
@Service
public class ModuleXmlRewriter {

    /**
     * 对所有候选 item 应用 replacementHtml，并重建 module.xml。
     */
    public String rewrite(ModuleXmlContent moduleXmlContent, List<ImportItemResult> items) {
        StringBuilder htmlContent = new StringBuilder(moduleXmlContent.getHtmlContent());
        List<ImportItemResult> candidates = collectReplaceCandidates(items);
        // 从右向左替换，避免后面片段长度变化影响前面片段的偏移。
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

    /**
     * 正式流程使用的替换方法，只依赖真实 workItemId 渲染后的 replacementHtml。
     */
    public String rewritePolarion(ModuleXmlContent moduleXmlContent, List<PolarionImportItemResult> items) {
        StringBuilder htmlContent = new StringBuilder(moduleXmlContent.getHtmlContent());
        List<PolarionImportItemResult> candidates = collectPolarionReplaceCandidates(items);
        Collections.sort(candidates, new Comparator<PolarionImportItemResult>() {
            @Override
            public int compare(PolarionImportItemResult left, PolarionImportItemResult right) {
                return right.getSourceStartIndex().compareTo(left.getSourceStartIndex());
            }
        });

        for (PolarionImportItemResult item : candidates) {
            if (!hasValidRange(item, htmlContent.length())) {
                markPolarionReplaceFailed(item, "Invalid source range for item replacement");
                continue;
            }
            htmlContent.replace(item.getSourceStartIndex(), item.getSourceEndIndex(), item.getReplacementHtml());
            item.setStatus(ItemStatus.REPLACED.name());
            item.setErrorMessage(null);
        }
        return moduleXmlContent.rebuild(htmlContent.toString());
    }

    /**
     * 过滤出已选中且已经准备好替换片段的 item。
     */
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

    /**
     * 校验保存的源偏移仍然指向当前 HTML 缓冲区内部。
     */
    private boolean hasValidRange(ImportItemResult item, int htmlLength) {
        return item.getSourceStartIndex() != null
                && item.getSourceEndIndex() != null
                && item.getSourceStartIndex() >= 0
                && item.getSourceEndIndex() <= htmlLength
                && item.getSourceStartIndex() < item.getSourceEndIndex();
    }

    private boolean hasValidRange(PolarionImportItemResult item, int htmlLength) {
        return item.getSourceStartIndex() != null
                && item.getSourceEndIndex() != null
                && item.getSourceStartIndex() >= 0
                && item.getSourceEndIndex() <= htmlLength
                && item.getSourceStartIndex() < item.getSourceEndIndex();
    }

    /**
     * 记录单个 item 的替换失败，但不中断整个任务。
     */
    private void markReplaceFailed(ImportItemResult item, String message) {
        item.setStatus(ItemStatus.REPLACE_FAILED.name());
        item.setErrorCode("PARAGRAPH_REPLACE_FAILED");
        item.setErrorMessage(message);
    }

    private List<PolarionImportItemResult> collectPolarionReplaceCandidates(List<PolarionImportItemResult> items) {
        List<PolarionImportItemResult> candidates = new ArrayList<PolarionImportItemResult>();
        for (PolarionImportItemResult item : items) {
            if (!ItemStatus.CREATED.name().equals(item.getStatus()) || !TextUtils.hasText(item.getReplacementHtml())) {
                continue;
            }
            candidates.add(item);
        }
        return candidates;
    }

    private void markPolarionReplaceFailed(PolarionImportItemResult item, String message) {
        item.setStatus(ItemStatus.REPLACE_FAILED.name());
        item.setErrorMessage(message);
    }
}
