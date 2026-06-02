package com.example.polarionprocessor.ai.prompt;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * classpath/file 提示词模板加载器。
 */
@Service
public class PromptTemplateLoader {

    private final ResourceLoader resourceLoader;
    private final Map<String, String> cache = new LinkedHashMap<String, String>();

    public PromptTemplateLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public synchronized String load(String location) {
        if (cache.containsKey(location)) {
            return cache.get(location);
        }
        try {
            Resource resource = resourceLoader.getResource(location);
            String text = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            cache.put(location, text);
            return text;
        } catch (IOException e) {
            throw new IllegalStateException("Prompt template load failed: " + location + ", " + e.getMessage(), e);
        }
    }
}
