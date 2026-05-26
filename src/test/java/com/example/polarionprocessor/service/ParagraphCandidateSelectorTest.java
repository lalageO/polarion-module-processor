package com.example.polarionprocessor.service;

import com.example.polarionprocessor.enums.ItemStatus;
import com.example.polarionprocessor.enums.SkipReason;
import com.example.polarionprocessor.model.ImportItemResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParagraphCandidateSelectorTest {

    private final ParagraphCandidateSelector selector = new ParagraphCandidateSelector();

    @Test
    void selectsRequirementParagraph() {
        ImportItemResult item = item("polarion_101", "5.1.1.", "5.1.1. The system shall operate safely.");

        selector.apply(item, 2, true, 80);

        assertTrue(item.getCandidate());
        assertEquals(ItemStatus.CANDIDATE.name(), item.getStatus());
    }

    @Test
    void skipsParagraphWithoutKeyword() {
        ImportItemResult item = item("polarion_102", "5.3.5.", "5.3.5. Response to System boundaries");

        selector.apply(item, 2, true, 80);

        assertFalse(item.getCandidate());
        assertEquals(SkipReason.NO_REQUIREMENT_KEYWORD.name(), item.getSkipReason());
    }

    @Test
    void skipsInvalidParagraphId() {
        ImportItemResult item = item("abc", "5.1.1.", "5.1.1. The system shall operate safely.");

        selector.apply(item, 2, true, 80);

        assertFalse(item.getCandidate());
        assertEquals(SkipReason.INVALID_PARAGRAPH_ID.name(), item.getSkipReason());
    }

    @Test
    void skipsShortLevelTwoTitle() {
        ImportItemResult item = item("polarion_103", "3.6.", "3.6. Verification");

        selector.apply(item, 2, false, 80);

        assertFalse(item.getCandidate());
        assertEquals(SkipReason.TITLE_ONLY.name(), item.getSkipReason());
    }

    private ImportItemResult item(String paragraphId, String outlineNo, String sourceText) {
        ImportItemResult item = new ImportItemResult();
        item.setParagraphId(paragraphId);
        item.setOutlineNo(outlineNo);
        item.setSourceText(sourceText);
        return item;
    }
}
