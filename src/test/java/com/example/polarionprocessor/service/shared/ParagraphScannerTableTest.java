package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.model.shared.ImportItemResult;
import com.example.polarionprocessor.model.shared.ParagraphInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParagraphScannerTableTest {

    private final ParagraphScanner scanner = new ParagraphScanner();
    private final NumberedItemGrouper grouper = new NumberedItemGrouper();

    @Test
    void marksParagraphsInsideTableAndExcludesThemFromWorkItemBlocks() {
        String html = "<p id=\"polarion_101\">4.1.1. First requirement shall be created.</p>"
                + "<table id=\"polarion_200\"><tbody><tr><td>"
                + "<p id=\"polarion_201\">4.2.5.1 table cell text shall not become work item.</p>"
                + "</td></tr></tbody></table>"
                + "<p id=\"polarion_102\">4.1.2. Second requirement shall be created.</p>";

        List<ParagraphInfo> paragraphs = scanner.scan(html);
        List<ImportItemResult> items = grouper.group("module", html, paragraphs);

        assertEquals(3, paragraphs.size());
        assertFalse(Boolean.TRUE.equals(paragraphs.get(0).getInsideTable()));
        assertTrue(Boolean.TRUE.equals(paragraphs.get(1).getInsideTable()));
        assertFalse(Boolean.TRUE.equals(paragraphs.get(2).getInsideTable()));
        assertEquals(2, items.size());
        assertEquals("polarion_101", items.get(0).getStartParagraphId());
        assertEquals("polarion_101", items.get(0).getEndParagraphId());
        assertFalse(items.get(0).getDescription().contains("table cell text"));
        assertFalse(items.get(0).getSourceOuterHtml().contains("<table"));
        assertEquals("polarion_102", items.get(1).getStartParagraphId());
    }
}
