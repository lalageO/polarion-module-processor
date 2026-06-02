package com.example.polarionprocessor.ai.parser;

import com.example.polarionprocessor.model.polarion.PolarionEnumOptionRequest;
import com.example.polarionprocessor.util.TextUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * AI 输出字段基础清洗与白名单校验。
 */
@Service
public class AiFieldValidator {

    private static final List<String> DEFAULT_REQUIREMENT_SOURCE_OPTIONS =
            Arrays.asList("internal", "external", "Regulation");
    private static final List<String> DEFAULT_REQ_TYPE_OPTIONS =
            Arrays.asList("functional", "nofunctional", "interface", "constraint", "information");

    public String validateTitle(Object rawTitle) {
        String title = normalizeText(rawTitle);
        if (!TextUtils.hasText(title)) {
            return null;
        }
        title = stripKnownPrefix(title);
        return TextUtils.truncateAtWordBoundary(title, 80);
    }

    public Map<String, Object> validateFields(Map<String, Object> parsed,
                                              Map<String, List<PolarionEnumOptionRequest>> enumOptions,
                                              boolean includeCustomFields) {
        Map<String, Object> fields = new LinkedHashMap<String, Object>();
        if (!includeCustomFields || parsed == null) {
            return fields;
        }
        String requirementSource = canonicalEnumValue(
                parsed.get("requirementsource"),
                optionIds(enumOptions, "requirementsource", DEFAULT_REQUIREMENT_SOURCE_OPTIONS));
        if (TextUtils.hasText(requirementSource)) {
            fields.put("requirementsource", requirementSource);
        }
        String reqType = canonicalEnumValue(
                parsed.get("reqType"),
                optionIds(enumOptions, "reqType", DEFAULT_REQ_TYPE_OPTIONS));
        if (TextUtils.hasText(reqType)) {
            fields.put("reqType", reqType);
        }
        String verificationCriteria = normalizeText(parsed.get("verificationcriteria"));
        if (TextUtils.hasText(verificationCriteria)) {
            fields.put("verificationcriteria", stripHtmlTags(verificationCriteria));
        }
        return fields;
    }

    private List<String> optionIds(Map<String, List<PolarionEnumOptionRequest>> enumOptions,
                                   String fieldId,
                                   List<String> fallback) {
        if (enumOptions == null || enumOptions.get(fieldId) == null || enumOptions.get(fieldId).isEmpty()) {
            return fallback;
        }
        java.util.ArrayList<String> ids = new java.util.ArrayList<String>();
        for (PolarionEnumOptionRequest option : enumOptions.get(fieldId)) {
            if (option != null && TextUtils.hasText(option.getId())) {
                ids.add(option.getId());
            }
        }
        return ids.isEmpty() ? fallback : ids;
    }

    private String canonicalEnumValue(Object rawValue, List<String> allowedValues) {
        String value = normalizeText(rawValue);
        if (!TextUtils.hasText(value)) {
            return null;
        }
        for (String allowedValue : allowedValues) {
            if (allowedValue != null && allowedValue.equalsIgnoreCase(value)) {
                return allowedValue;
            }
        }
        return null;
    }

    private String normalizeText(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value)
                .replace('\u00A0', ' ')
                .replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    private String stripKnownPrefix(String title) {
        String value = title;
        String lower = value.toLowerCase(Locale.ROOT);
        String[] prefixes = new String[]{"标题：", "标题:", "需求：", "需求:", "work item:", "work item："};
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix.toLowerCase(Locale.ROOT))) {
                return value.substring(prefix.length()).trim();
            }
        }
        return value;
    }

    private String stripHtmlTags(String value) {
        return value == null ? null : value.replaceAll("<[^>]+>", "").trim();
    }
}
