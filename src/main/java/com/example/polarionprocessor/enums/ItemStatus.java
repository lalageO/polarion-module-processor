package com.example.polarionprocessor.enums;

/**
 * 单个 item 的处理状态，会写入 JSON 和 CSV 输出。
 */
public enum ItemStatus {
    /** 已识别，等待创建 Work Item。 */
    READY,

    /** 未被选为候选 item。 */
    SKIPPED,

    /** 已通过候选筛选，等待替换或导入。 */
    CANDIDATE,

    /** 正在创建 Work Item。 */
    CREATING,

    /** Work Item 创建成功，已拿到真实 workItemId。 */
    CREATED,

    /** Work Item 创建失败。 */
    CREATE_FAILED,

    /** 已在 processed_module.xml 中完成替换。 */
    REPLACED,

    /** 已被选中，但替换失败。 */
    REPLACE_FAILED,

    /** 预留的通用失败状态，用于后续处理步骤。 */
    FAILED
}
