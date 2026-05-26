package com.example.polarionprocessor.service;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.enums.TitleMode;
import com.example.polarionprocessor.model.ImportItemResult;
import com.example.polarionprocessor.model.ModuleProcessRequest;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

@Service
public class RuleBasedTitleGenerator implements TitleGenerator {

    private static final int SOURCE_PREFIX_LENGTH = 40;

    private final ModuleProcessorProperties properties;

    public RuleBasedTitleGenerator(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

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
