package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.enums.PolarionItemRole;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleWorkItemMacroRendererTest {

    private final ModuleWorkItemMacroRenderer renderer = new ModuleWorkItemMacroRenderer();

    @Test
    void rendersHeadingWithHtmlHeadingLevelFromOutlineDepth() {
        assertEquals(
                "<h2 id=\"polarion_wiki macro name=module-workitem;params=id=FDP-H1\"></h2>",
                renderer.render(heading("FDP-H1", 1)));
        assertEquals(
                "<h3 id=\"polarion_wiki macro name=module-workitem;params=id=FDP-H2\"></h3>",
                renderer.render(heading("FDP-H2", 2)));
        assertEquals(
                "<h6 id=\"polarion_wiki macro name=module-workitem;params=id=FDP-H7\"></h6>",
                renderer.render(heading("FDP-H7", 7)));
    }

    @Test
    void rendersRequirementWithDivMacro() {
        PolarionImportItemResult item = new PolarionImportItemResult();
        item.setItemRole(PolarionItemRole.REQUIREMENT.name());
        item.setWorkItemId("FDP-R1");

        assertEquals(
                "<div id=\"polarion_wiki macro name=module-workitem;params=id=FDP-R1\"></div>",
                renderer.render(item));
    }

    private PolarionImportItemResult heading(String workItemId, int outlineDepth) {
        PolarionImportItemResult item = new PolarionImportItemResult();
        item.setItemRole(PolarionItemRole.HEADING.name());
        item.setOutlineDepth(outlineDepth);
        item.setWorkItemId(workItemId);
        return item;
    }
}
