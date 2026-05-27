package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.config.ModuleProcessorProperties;
import com.example.polarionprocessor.enums.TitleMode;
import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.model.debug.ModuleProcessRequest;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

/**
 * 真实 LLM 集成前使用的本地标题生成器。
 */
@Service
public class RuleBasedTitleGenerator implements TitleGenerator {

    /** 规则标题模式在最终长度限制前使用的源文本字符数。 */
    private static final int SOURCE_PREFIX_LENGTH = 40;

    private final ModuleProcessorProperties properties;

    public RuleBasedTitleGenerator(ModuleProcessorProperties properties) {
        this.properties = properties;
    }

    /**
     * 根据分组后的源文本和请求 titleMode 生成确定性标题。
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

        // 默认模式先从正文中移除条款号，再拼回规范化后的条款号前缀。
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
