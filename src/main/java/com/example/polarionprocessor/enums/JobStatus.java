package com.example.polarionprocessor.enums;

/**
 * 正式导入任务的整体状态。
 */
public enum JobStatus {
    /** 请求已接收，后台任务已提交。 */
    SUBMITTED,

    /** item 已识别并写入初始结果。 */
    ITEMS_READY,

    /** 正在顺序创建 Work Item。 */
    CREATING_WORK_ITEMS,

    /** 正在重写 XML。 */
    REWRITING_XML,

    /** 全部流程完成且无失败。 */
    COMPLETED,

    /** 流程完成，但存在创建或替换失败的 item。 */
    COMPLETED_WITH_ERRORS,

    /** 顶层流程失败。 */
    FAILED
}
