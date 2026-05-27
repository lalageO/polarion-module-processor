package com.example.polarionprocessor.service.polarion;

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
}
