package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.enums.TitleMode;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ModuleProcessRequest;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

/**
 * Local title generator used before a real LLM integration exists.
 */
@Service
public class RuleBasedTitleGenerator implements TitleGenerator {

    /** Number of source characters used by rule-based title modes before max title limiting. */
    private static final int SOURCE_PREFIX_LENGTH = 40;

    private final ModuleProcessorProperties properties;

    public RuleBasedTitleGenerator(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    /**
     * Generates a deterministic title from the grouped source text and request titleMode.
     */
    @Override
    public String generate(ImportItemResult item, ModuleProcessRequest request) {
        TitleMode titleMode = TitleMode.from(request.getTitleMode());
        if (TitleMode.MOCK.equals(titleMode)) {
            return "Mock requirement title";
        }

        String sourceText = TextUtils.normalizeSpaces(item.getSourceText());
        if (TitleMode.SOURCE_PREFIX.equals(titleMode)) {
            return limit(TextUtils.truncateAtWordBoundary(sourceText, SOURCE_PREFIX_LENGTH));
        }

        // Default mode removes the clause number from the body and adds back a normalized prefix.
        String textWithoutOutline = TextUtils.removeLeadingOutlineNo(sourceText, item.getOutlineNo());
        String titleBody = TextUtils.truncateAtWordBoundary(textWithoutOutline, SOURCE_PREFIX_LENGTH);
        String outlineNo = item.getOutlineNo();
        if (TextUtils.hasText(outlineNo) && outlineNo.endsWith(".")) {
            outlineNo = outlineNo.substring(0, outlineNo.length() - 1);
        }
        String title = TextUtils.hasText(outlineNo)
                ? outlineNo + " " + titleBody
                : titleBody;
        return limit(title);
    }

    private String limit(String title) {
        Integer maxLength = properties.getTitleMaxLength();
        int limit = maxLength == null || maxLength <= 0 ? 80 : maxLength;
        return TextUtils.truncateAtWordBoundary(TextUtils.normalizeSpaces(title), limit);
    }
}
