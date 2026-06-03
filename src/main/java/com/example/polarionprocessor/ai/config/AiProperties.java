package com.example.polarionprocessor.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 字段生成配置，默认关闭，避免未配置模型时影响正式链路。
 */
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** 是否启用 AI 生成。 */
    private Boolean enabled = false;

    /** 兼容 OpenAI Chat Completions 的提供方标识，当前只按 openai-compatible 处理。 */
    private String provider = "openai-compatible";

    /** OpenAI-compatible API 基础地址，例如 http://llm.freetech.com/v1。 */
    private String baseUrl = "http://llm.freetech.com/v1";

    /** Chat Completions 路径。 */
    private String chatCompletionsPath = "/chat/completions";

    /** 模型名称。 */
    private String model = "primary";

    /** 可选 Bearer Token。公司内部网关不需要时保持空。 */
    private String apiKey = "";

    private Integer connectTimeoutMs = 5000;

    private Integer readTimeoutMs = 60000;

    private Integer maxRetries = 1;

    /** 单次失败重试前等待时间。 */
    private Integer retryIntervalMs = 1000;

    /** 每个 item 调用模型后等待时间，用于规避模型网关频控。 */
    private Integer requestIntervalMs = 0;

    private Double temperature = 0.2D;

    /** 是否请求模型按 JSON object 输出；部分 OpenAI-compatible 服务不支持，默认关闭。 */
    private Boolean responseFormatJson = false;

    /** dryRun 时是否也调用 AI。默认 false，避免预览请求产生模型成本。 */
    private Boolean runInDryRun = false;

    /** 普通项目标题生成提示词。 */
    private String titlePromptPath = "classpath:prompts/workitem-title-prompt.txt";

    /** 特例项目标题和自定义字段生成提示词。 */
    private String fieldPromptPath = "classpath:prompts/rmt-workitem-fields-prompt.txt";

    /** 需要生成额外字段的项目；未命中时只生成标题。 */
    private List<String> fieldGenerationProjects = new ArrayList<String>();

    private Debug debug = new Debug();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getChatCompletionsPath() {
        return chatCompletionsPath;
    }

    public void setChatCompletionsPath(String chatCompletionsPath) {
        this.chatCompletionsPath = chatCompletionsPath;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Integer getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(Integer connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public Integer getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(Integer readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(Integer retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public Integer getRequestIntervalMs() {
        return requestIntervalMs;
    }

    public void setRequestIntervalMs(Integer requestIntervalMs) {
        this.requestIntervalMs = requestIntervalMs;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Boolean getResponseFormatJson() {
        return responseFormatJson;
    }

    public void setResponseFormatJson(Boolean responseFormatJson) {
        this.responseFormatJson = responseFormatJson;
    }

    public Boolean getRunInDryRun() {
        return runInDryRun;
    }

    public void setRunInDryRun(Boolean runInDryRun) {
        this.runInDryRun = runInDryRun;
    }

    public String getTitlePromptPath() {
        return titlePromptPath;
    }

    public void setTitlePromptPath(String titlePromptPath) {
        this.titlePromptPath = titlePromptPath;
    }

    public String getFieldPromptPath() {
        return fieldPromptPath;
    }

    public void setFieldPromptPath(String fieldPromptPath) {
        this.fieldPromptPath = fieldPromptPath;
    }

    public List<String> getFieldGenerationProjects() {
        return fieldGenerationProjects;
    }

    public void setFieldGenerationProjects(List<String> fieldGenerationProjects) {
        this.fieldGenerationProjects = fieldGenerationProjects == null
                ? new ArrayList<String>()
                : fieldGenerationProjects;
    }

    public Debug getDebug() {
        return debug;
    }

    public void setDebug(Debug debug) {
        this.debug = debug == null ? new Debug() : debug;
    }

    public static class Debug {

        /** 是否写出 ai_debug.jsonl。 */
        private Boolean enabled = true;

        /** AI 调试文件名；JSON Lines，一行对应一次 item 调用。 */
        private String fileName = "ai_debug.jsonl";

        /** 是否保存原始模型响应。 */
        private Boolean storeRawResponse = true;

        /** prompt 写入调试文件时的最大长度。 */
        private Integer promptMaxLength = 4000;

        /** raw response 写入调试文件时的最大长度。 */
        private Integer rawResponseMaxLength = 4000;

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public Boolean getStoreRawResponse() {
            return storeRawResponse;
        }

        public void setStoreRawResponse(Boolean storeRawResponse) {
            this.storeRawResponse = storeRawResponse;
        }

        public Integer getPromptMaxLength() {
            return promptMaxLength;
        }

        public void setPromptMaxLength(Integer promptMaxLength) {
            this.promptMaxLength = promptMaxLength;
        }

        public Integer getRawResponseMaxLength() {
            return rawResponseMaxLength;
        }

        public void setRawResponseMaxLength(Integer rawResponseMaxLength) {
            this.rawResponseMaxLength = rawResponseMaxLength;
        }
    }
}
