package com.example.polarionprocessor.service.shared;

import com.example.polarionprocessor.model.shared.ModuleXmlContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleXmlExtractorTest {

    private final ModuleXmlExtractor extractor = new ModuleXmlExtractor();

    @Test
    void extractHomePageContentCdata() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<module>\n"
                + "  <field id=\"homePageContent\" text-type=\"text/html\"><![CDATA[<p id=\"polarion_1\">5.1.1. The system shall run.</p>]]></field>\n"
                + "</module>";

        ModuleXmlContent content = extractor.extract(xml);

        assertEquals("<p id=\"polarion_1\">5.1.1. The system shall run.</p>", content.getHtmlContent());
        assertTrue(content.rebuild(content.getHtmlContent()).contains("<![CDATA["));
    }

    @Test
    void failWhenHomePageContentMissing() {
        ModuleProcessException exception = assertThrows(
                ModuleProcessException.class,
                () -> extractor.extract("<module><field id=\"title\">R171e2</field></module>"));

        assertEquals("HOME_PAGE_CONTENT_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void failWhenCdataMissing() {
        ModuleProcessException exception = assertThrows(
                ModuleProcessException.class,
                () -> extractor.extract("<module><field id=\"homePageContent\"><p>text</p></field></module>"));

        assertEquals("CDATA_NOT_FOUND", exception.getErrorCode());
    }
}
