package com.example.polarionprocessor.service;

import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.enums.SkipReason;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ParagraphCandidateSelector {

    private static final Pattern PARAGRAPH_ID_PATTERN = Pattern.compile("^polarion_\\d+$");
    private static final List<String> REQUIREMENT_KEYWORDS = Arrays.asList(
            "shall",
            "should",
            "must",
            "may",
            "is required to",
            "are required to",
            "shall be designed",
            "shall aim to",
            "is intended to",
            "are intended to"
    );

    public void apply(ImportItemResult item, int minOutlineDepth, boolean requireKeyword, int levelTwoMinTextLength) {
        String sourceText = item.getSourceText();
        if (!TextUtils.hasText(sourceText)) {
            skip(item, SkipReason.EMPTY_PARAGRAPH);
            return;
        }
        if (!TextUtils.hasText(item.getParagraphId())) {
            skip(item, SkipReason.NO_PARAGRAPH_ID);
            return;
        }
        if (!PARAGRAPH_ID_PATTERN.matcher(item.getParagraphId()).matches()) {
            skip(item, SkipReason.INVALID_PARAGRAPH_ID);
            return;
        }
        if (!TextUtils.hasText(item.getOutlineNo())) {
            if (TextUtils.looksLikeTitleOnly(sourceText)) {
                skip(item, SkipReason.TITLE_ONLY);
            } else {
                skip(item, SkipReason.NO_OUTLINE_NO);
            }
            return;
        }
        int outlineDepth = TextUtils.outlineDepth(item.getOutlineNo());
        if (outlineDepth < minOutlineDepth) {
            skip(item, SkipReason.OUTLINE_DEPTH_TOO_LOW);
            return;
        }
        if (outlineDepth == 2 && sourceText.length() < levelTwoMinTextLength) {
            skip(item, SkipReason.TITLE_ONLY);
            return;
        }
        if (requireKeyword && !containsRequirementKeyword(sourceText)) {
            skip(item, SkipReason.NO_REQUIREMENT_KEYWORD);
            return;
        }

        item.setCandidate(true);
        item.setSkipReason(null);
        item.setStatus(ItemStatus.CANDIDATE.name());
    }

    private void skip(ImportItemResult item, SkipReason skipReason) {
        item.setCandidate(false);
        item.setSkipReason(skipReason.name());
        item.setStatus(ItemStatus.SKIPPED.name());
    }

    private boolean containsRequirementKeyword(String sourceText) {
        String lowerText = sourceText.toLowerCase(Locale.ROOT);
        for (String keyword : REQUIREMENT_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
