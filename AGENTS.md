# AGENTS.md

本仓库用于开发 `Polarion module.xml 本地解析与替换模块`。后续开发请优先遵守本文约定；需求来源见根目录 `需求说明文档.md`。

## 项目定位

- 这是一个独立的 Java/Spring Boot 工具模块，用于本地处理 Polarion 导出的 `module.xml`。
- 第一版只跑通本地闭环：上传 `module.xml`，提取 `homePageContent` CDATA 内 HTML，识别 `<p>` 段落，筛选候选需求，生成 JSON/CSV，并在 mock 模式下把候选 `<p>` 替换为 `module-workitem` div。
- 第一版不接入公司 FDP、不拉取 Polarion URL、不处理登录认证、不调用真实 Polarion API、不调用真实 LLM、不写回 Polarion、不建库、不做前端页面。

## 第一版验收输出

处理一次上传后，应在 `output/{jobId}/` 下生成：

```text
output/{jobId}/
  original_module.xml
  processed_module.xml
  import_result.json
  import_preview.csv
```

- `original_module.xml`：原始上传文件备份。
- `processed_module.xml`：替换候选 `<p>` 后的新 XML；dry-run 可不生成，或与原文件一致。
- `import_result.json`：程序可读的任务状态账本。
- `import_preview.csv`：人工可读的段落预览和验收表。

mock 替换的目标格式必须严格为：

```html
<div id="polarion_wiki macro name=module-workitem;params=id=MOCK-000001"></div>
```

## 推荐技术栈

- Java 8+。
- Spring Boot Web 提供上传处理接口。
- Jsoup 只用于解析 CDATA 内 HTML 片段、扫描 `<p>` 和提取文本。
- Jackson 输出 JSON。
- Apache Commons CSV 或 OpenCSV 输出 CSV。
- SHA-256 生成文本和原始 HTML 片段 hash。
- Java NIO 做文件读写。

如项目不使用 Lombok，DTO 手写 getter/setter。

## 推荐目录结构

```text
polarion-module-processor/
  pom.xml
  src/main/java/com/example/polarionprocessor/
    PolarionModuleProcessorApplication.java
    controller/
      ModuleProcessController.java
      PolarionModuleImportController.java
    service/
      debug/
        ModuleProcessService.java
        WorkItemIdProvider.java
        MockWorkItemIdProvider.java
        ImportResultWriter.java
        ImportPreviewCsvWriter.java
      shared/
        ModuleXmlExtractor.java
        ParagraphScanner.java
        NumberedItemGrouper.java
        ParagraphCandidateSelector.java
        TitleGenerator.java
        RuleBasedTitleGenerator.java
        ModuleXmlRewriter.java
        ModuleProcessException.java
      polarion/
        PolarionModuleImportService.java
        PolarionModuleUrlParser.java
        ModuleXmlDownloader.java
        PolarionWorkItemCreator.java
        HttpPolarionWorkItemCreator.java
        ModuleWorkItemMacroRenderer.java
        PolarionImportResultWriter.java
        PolarionImportPreviewCsvWriter.java
    model/
      debug/
        ModuleProcessRequest.java
        ModuleProcessResponse.java
        ImportJobResult.java
      shared/
        ImportItemResult.java
        ModuleXmlContent.java
        ParagraphInfo.java
      polarion/
        PolarionModuleLocation.java
        PolarionModuleImportRequest.java
        PolarionModuleImportResponse.java
        PolarionImportJobResult.java
        PolarionImportItemResult.java
        PolarionImportFiles.java
        PolarionImportSummary.java
        WorkItemCreateRequest.java
        WorkItemCreateResult.java
    enums/
      ReplaceMode.java
      ItemStatus.java
      SkipReason.java
      JobStatus.java
    util/
      HashUtils.java
      FileUtils.java
      TextUtils.java
  src/main/resources/
    application.yml
```

当前代码按链路拆分包：

- `model.debug`、`service.debug`：第一版本地上传调试链路，只服务 `/api/module/process`。
- `model.polarion`、`service.polarion`：第二版正式 Polarion 导入链路，只服务 `/api/polarion/module/import`。
- `model.shared`、`service.shared`：两条链路复用的 XML 提取、段落扫描、数字条款聚合、候选筛选、标题生成和 CDATA 重写能力。
- 后续新增代码优先放入对应链路包；只有确实被新旧两条链路共同使用的类，才放入 `shared`。

## API 约定

第一版接口：

```text
POST /api/module/process
Content-Type: multipart/form-data
```

请求参数：

- `file`：必填，上传的 `module.xml`。
- `moduleName`：可选，默认从 XML title 推断。
- `dryRun`：可选，默认 `true`。
- `replaceMode`：可选，默认 `NONE`。
- `mockIdPrefix`：可选，默认 `MOCK`。
- `includeSubItems`：可选，默认 `false`。
- `requireKeyword`：可选，默认 `false`；需要严格关键词过滤时再显式传 `true`。
- `minOutlineDepth`：可选，默认 `2`。
- `titleMode`：可选，默认 `RULE_BASED`。
- 层级 2 的数字条款默认要求合并后的描述长度不少于 `level-two-min-text-length`，用于过滤短章节标题。

`replaceMode` 第一版必须支持：

- `NONE`：不替换 XML，只生成 JSON 和 CSV。
- `MOCK`：生成 mock Work Item ID 并替换候选 `<p>`。

`MAPPING` 仅预留接口，第一版非必须实现。

## 处理流程

dry-run：

1. 保存 `original_module.xml`。
2. 读取 XML 原文字符串。
3. 提取 `homePageContent` CDATA。
4. 用 Jsoup 解析 CDATA 内 HTML 片段。
5. 扫描全部 `<p>`。
6. 提取段落字段、hash 和候选状态。
7. 生成规则标题。
8. 输出 `import_result.json` 和 `import_preview.csv`。
9. 不修改 XML 逻辑内容。

mock 替换：

1. 保存原始 XML。
2. 提取 CDATA 内 HTML。
3. 扫描并筛选候选 `<p>`。
4. 为候选段落生成 `MOCK-000001` 形式 ID。
5. 生成 `replacementHtml`。
6. 在原始 HTML 字符串中替换完整 `<p>...</p>`。
7. 把新 HTML 写回原 XML 的 CDATA 中。
8. 输出 processed XML、JSON、CSV。

## XML 与 HTML 处理规则

- 不要整体重新序列化 `module.xml`，避免破坏 Polarion 文件格式。
- 原始 XML 应作为字符串读取，只替换 `homePageContent` CDATA 内部内容。
- 不改动其他 `<field>`，不改动非目标段落。
- 提取 CDATA 推荐正则：

```regex
(?s)(<field\s+id="homePageContent"[^>]*>\s*<!\[CDATA\[)(.*?)(\]\]>\s*</field>)
```

- group 1 是 CDATA 前缀，group 2 是 HTML 内容，group 3 是 CDATA 后缀。
- 找不到 `homePageContent` 或 CDATA 时，任务失败。
- 不要把 CDATA 内容 XML 转义成普通 `<p>` 文本。
- Jsoup 只负责识别和提取，不要把 Jsoup 序列化后的 HTML 直接作为最终输出。

替换单个段落时，回到原始 `htmlContent` 字符串中按 `paragraphId` 定位：

```regex
(?s)<p\b(?=[^>]*\bid=["']polarion_162["'])[^>]*>.*?</p>
```

默认假设 `<p>` 内不会嵌套另一个 `<p>`，并且每个 `paragraphId` 唯一。

## 段落字段

每个 `<p>` 应生成一个 `ImportItemResult`，至少包含：

- `seq`
- `paragraphId`
- `outlineNo`
- `paragraphKey`
- `sourceText`
- `sourceTextHash`
- `sourceOuterHtml`
- `sourceOuterHtmlHash`
- `candidate`
- `skipReason`
- `generatedTitle`
- `finalTitle`
- `workItemId`
- `replacementHtml`
- `status`
- `errorCode`
- `errorMessage`

`sourceOuterHtml` 可不写入 CSV，但应参与 hash 和排查。

## 候选筛选规则

默认候选要求：

- `<p>` 有 id，且格式类似 `polarion_162`。
- 文本非空。
- 文本以数字条款号开头，例如 `4.4.`、`5.1.1.`、`5.3.2.1.`。
- 条款号层级大于等于 `minOutlineDepth`，默认 `2`。
- 当 `requireKeyword=true` 时，文本必须包含需求关键词。

条款号提取正则：

```regex
^\s*(\d+(?:\.\d+)+\.?)
```

默认关键词大小写不敏感：

```text
shall
should
must
may
is required to
are required to
shall be designed
shall aim to
is intended to
are intended to
```

跳过原因使用固定枚举值：

- `EMPTY_PARAGRAPH`
- `NO_PARAGRAPH_ID`
- `INVALID_PARAGRAPH_ID`
- `NO_OUTLINE_NO`
- `OUTLINE_DEPTH_TOO_LOW`
- `NO_REQUIREMENT_KEYWORD`
- `TITLE_ONLY`
- `TOC`
- `NOT_REQUIREMENT_LIKE`

第一版可以不做复杂目录识别，优先通过条款层级和关键词过滤。

## 标题生成

第一版不调用真实 LLM，默认 `RULE_BASED`：

1. 去掉开头条款号。
2. 清理多余空格。
3. 截取前 40 个字符。
4. 拼回条款号。
5. 标题最大长度默认 `80` 字符。

`titleMode`：

- `RULE_BASED`：默认实现。
- `SOURCE_PREFIX`：使用原文前若干字符。
- `MOCK`：固定 mock 标题。
- `LLM`：只预留，不实现真实调用。

## Work Item ID

第一版使用 mock ID：

```text
{mockIdPrefix}-{6位序号}
```

默认：

```text
MOCK-000001
MOCK-000002
MOCK-000003
```

请保留扩展接口：

```java
public interface WorkItemIdProvider {
    String provide(ImportItemResult item, int index);
}
```

默认实现为 `MockWorkItemIdProvider`，后续真实接入再增加 `PolarionWorkItemIdProvider`。

## 输出文件格式

`import_result.json` 需要记录：

- `jobId`
- `moduleName`
- `mode`
- `dryRun`
- `createdAt`
- `updatedAt`
- `sourceXmlHash`
- `totalParagraphCount`
- `candidateCount`
- `replacedCount`
- `skippedCount`
- `failedCount`
- `items`

`import_preview.csv` 字段顺序固定：

```text
seq
paragraphId
outlineNo
candidate
skipReason
sourceText
generatedTitle
finalTitle
workItemId
status
errorMessage
```

CSV 建议 UTF-8，可按配置输出 BOM 以方便 Excel 打开。

## 状态与错误码

第一版 item status 可简化为：

- `SKIPPED`
- `CANDIDATE`
- `REPLACED`
- `REPLACE_FAILED`
- `FAILED`

关键错误码：

- `HOME_PAGE_CONTENT_NOT_FOUND`
- `CDATA_NOT_FOUND`
- `HTML_PARSE_FAILED`
- `PARAGRAPH_REPLACE_FAILED`

某个 `<p>` 替换失败时，不应中断整个任务；当前 item 标记为 `REPLACE_FAILED`，继续处理其他 item，并增加 `failedCount`。

## application.yml 默认配置

建议保留以下配置项：

```yaml
server:
  port: 8080

module-processor:
  output-dir: output
  default-module-name: unknown-module
  default-replace-mode: NONE
  default-mock-id-prefix: MOCK
  default-min-outline-depth: 2
  default-require-keyword: false
  default-include-sub-items: false
  level-two-min-text-length: 80
  title-max-length: 80
  csv-with-bom: true
```

## 开发顺序

1. 项目骨架：Spring Boot 项目、上传接口、文件保存、基础响应。
2. CDATA 提取：读取 XML 原文，定位 `homePageContent`，提取 CDATA，处理异常。
3. 段落扫描：引入 Jsoup，扫描 `<p>`，提取文本、ID、条款号、hash。
4. 候选判断：实现筛选规则和 `skipReason`。
5. JSON/CSV 输出：字段完整、顺序固定、UTF-8 编码。
6. mock 替换：生成 ID、生成 `replacementHtml`、替换原始 HTML、写回 CDATA。
7. 测试修正：用真实 `module.xml` 测试，检查 CSV 和 XML diff。

## 验收重点

dry-run：

- 返回 `success=true`。
- 输出原始 XML、JSON、CSV。
- 不出现 mock Work Item ID。
- CSV 能看到候选段落和跳过原因。

mock 替换：

- 返回 `success=true`。
- 输出 `processed_module.xml`。
- 候选 `<p>` 被替换为严格格式的 `module-workitem` div。
- 非候选段落保持不变。
- JSON/CSV 记录每条段落状态和 mock ID。

文件格式：

- 输出 XML 保持 UTF-8。
- `homePageContent` 仍是 CDATA。
- 非 `homePageContent` 字段不被重写。
- 原始 XML 外层结构不应明显变化。

## 必测场景

- 正常 dry-run。
- 正常 mock 替换。
- 缺少 `homePageContent`。
- 缺少 CDATA。
- 空段落 `<p><br></p>`。
- 章节标题 `5. General Specifications`。
- 需求段落 `5.1.1. The system shall...`。
- 不含关键词的条款 `5.3.5. Response to System boundaries`。
- 单个 paragraphId 找不到导致替换失败。
- 多个候选段落 mock ID 连续生成。
- Excel 打开 CSV 不乱码。
- 1000+ `<p>` 大文件正常处理。

## 协作注意事项

- 需求说明文档是当前权威输入；变更范围不明确时先回到 `需求说明文档.md` 对齐。
- 不要把 `/init` 理解成直接实现业务代码；当前任务优先产出项目地图、约束和后续开发指南。
- 修改前先查看 `git status --short`，不要覆盖用户已有改动。
- 生成或修改示例 XML 时不要提交真实敏感数据。
- 输出目录 `output/` 属于运行产物，后续项目初始化时应加入 `.gitignore`。
- Windows PowerShell 可能显示中文文件名或内容乱码，优先用 UTF-8 读取验证真实文件内容。

## 第二版正式链路约定

- `/api/module/process` 是保留的本地调试接口，已标记为 deprecated；它继续支持上传本地 `module.xml`、`replaceMode=MOCK` 和 `mockIdPrefix`，只用于验证 XML 解析、候选识别和 mock 替换。
- `/api/polarion/module/import` 是正式业务接口；它支持直接接收纯 URL body，也兼容带 `moduleUrl` 的 JSON，并在 Controller 层解析出 `baseUrl`、`projectId`、`moduleFolder`、`moduleName`。
- `moduleUrl` 示例：`http://alm.freetech.com/polarion/#/project/FDP_Demo/wiki/10%20Stakeholder%20Requirement/R171e`。
- 正式链路通过 SVN 命令拉取 `module.xml`：先 `svn co {baseUrl}/repo/{projectId}/modules/{moduleFolder}/{moduleName}/ --depth=empty`，再在 checkout 目录内执行 `svn up module.xml`。
- SVN 用户名、密码、可执行文件名和超时时间放在 `polarion.svn` 配置下；不要在代码中硬编码真实密码。
- 正式接口不暴露 `replaceMode`，不接受 `mockIdPrefix`，不得生成 `FDP-000001`、`MOCK-000001` 等本地假 ID。
- 正式接口的 Work Item ID 只能来自 `PolarionWorkItemCreator` 返回的真实 Polarion API 结果；创建成功后必须立即原子写入 `import_result.json`。
- 当前 LLM 未接入，标题继续使用 `RuleBasedTitleGenerator` 规则生成。
- 当前 Polarion Work Item API 具体参数未知，使用 `WorkItemCreateRequest.fields` 动态承接 `requirementsource`、`status`、`ASIL`、`CAL`、`targetVersion`、`assignee`、`reference` 等扩展字段。
- 正式流程 dry-run 只下载、解析、识别并输出 `original_module.xml`、`import_result.json`、`import_preview.csv`，不创建 Work Item，不输出 `processed_module.xml`。
