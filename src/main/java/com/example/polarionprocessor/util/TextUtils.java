package com.example.polarionprocessor.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    /**
     * Item anchors must contain at least two numeric levels, for example 2.1 or 3.6.5.9.4.
     */
    private static final Pattern OUTLINE_PATTERN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)+\\.?)");

    /**
     * Top-level headings such as "2. Definitions" are not item anchors, but they must end the previous item block.
     */
    private static final Pattern SECTION_HEADING_PATTERN = Pattern.compile("^\\s*(\\d+)\\.\\s+\\D.*");
    private static final Pattern TITLE_ONLY_PATTERN = Pattern.compile("^\\s*\\d+\\.\\s+.+");

    private TextUtils() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static String normalizeSpaces(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    public static String extractOutlineNo(String text) {
        Matcher matcher = OUTLINE_PATTERN.matcher(text == null ? "" : text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    public static String extractSectionNo(String text) {
        Matcher matcher = SECTION_HEADING_PATTERN.matcher(text == null ? "" : text);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1) + ".";
    }

    public static boolean looksLikeTitleOnly(String text) {
        return TITLE_ONLY_PATTERN.matcher(text == null ? "" : text).matches();
    }

    public static int outlineDepth(String outlineNo) {
        if (!hasText(outlineNo)) {
            return 0;
        }
        String normalized = outlineNo.trim();
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isEmpty()) {
            return 0;
        }
        return normalized.split("\\.").length;
    }

    public static String removeLeadingOutlineNo(String text, String outlineNo) {
        String normalized = normalizeSpaces(text);
        if (!hasText(outlineNo)) {
            return normalized;
        }
        if (normalized.startsWith(outlineNo)) {
            return normalizeSpaces(normalized.substring(outlineNo.length()));
        }
        String noTrailingDot = outlineNo.endsWith(".")
                ? outlineNo.substring(0, outlineNo.length() - 1)
                : outlineNo;
        if (normalized.startsWith(noTrailingDot)) {
            return normalizeSpaces(normalized.substring(noTrailingDot.length()));
        }
        return normalized;
    }

    public static String truncateAtWordBoundary(String value, int maxLength) {
        if (value == null || maxLength <= 0 || value.length() <= maxLength) {
            return value;
        }
        String cut = value.substring(0, maxLength).trim();
        int lastSpace = cut.lastIndexOf(' ');
        if (lastSpace > 0 && lastSpace > maxLength / 2) {
            return cut.substring(0, lastSpace).trim();
        }
        return cut;
    }

    public static String sanitizePathPart(String value) {
        String normalized = normalizeSpaces(value);
        if (normalized.isEmpty()) {
            return "unknown-module";
        }
        return normalized.replaceAll("[^A-Za-z0-9._-]+", "_");
    }
}
