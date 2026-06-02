package com.example.polarionprocessor.ai.model;

/**
 * 模型接口的归一化响应。
 */
public class AiChatResponse {

    private String model;
    private String content;
    private String rawResponse;
    private AiUsage usage;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public AiUsage getUsage() {
        return usage;
    }

    public void setUsage(AiUsage usage) {
        this.usage = usage;
    }
}
