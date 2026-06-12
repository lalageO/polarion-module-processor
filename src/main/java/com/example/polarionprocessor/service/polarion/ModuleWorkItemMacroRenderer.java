package com.example.polarionprocessor.service.polarion;

import com.example.polarionprocessor.enums.PolarionItemRole;
import com.example.polarionprocessor.model.polarion.PolarionImportItemResult;
import org.springframework.stereotype.Service;

/**
 * 统一渲染 Polarion module-workitem 宏。
 */
@Service
public class ModuleWorkItemMacroRenderer {

    /**
     * 根据真实 workItemId 生成替换用 div。
     */
    public String render(String workItemId) {
        return "<div id=\"polarion_wiki macro name=module-workitem;params=id="
                + workItemId
                + "\"></div>";
    }

    public String render(PolarionImportItemResult item) {
        String workItemId = item == null ? null : item.getWorkItemId();
        if (item == null || !PolarionItemRole.HEADING.name().equals(item.getItemRole())) {
            return render(workItemId);
        }
        int headingLevel = headingLevel(item.getOutlineDepth());
        return "<h" + headingLevel + " id=\"polarion_wiki macro name=module-workitem;params=id="
                + workItemId
                + "\"></h" + headingLevel + ">";
    }

    private int headingLevel(Integer outlineDepth) {
        if (outlineDepth == null || outlineDepth < 1) {
            return 2;
        }
        return Math.min(outlineDepth + 1, 6);
    }
}
