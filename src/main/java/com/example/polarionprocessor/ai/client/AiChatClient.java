package com.example.polarionprocessor.ai.client;

import com.example.polarionprocessor.ai.model.AiChatResponse;

/**
 * AI Chat Completions 客户端抽象。
 */
public interface AiChatClient {

    AiChatResponse chat(String prompt);
}
