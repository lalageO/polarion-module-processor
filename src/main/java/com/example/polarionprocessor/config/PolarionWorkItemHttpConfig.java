package com.example.polarionprocessor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 公司内部 Polarion Work Item API 的 HTTP 客户端配置。
 */
@Configuration
public class PolarionWorkItemHttpConfig {

    @Bean
    public RestTemplate polarionWorkItemRestTemplate(PolarionProperties properties) {
        PolarionProperties.WorkItemApi api = properties.getWorkItemApi();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(valueOrDefault(api == null ? null : api.getConnectTimeoutMs(), 5000));
        requestFactory.setReadTimeout(valueOrDefault(api == null ? null : api.getReadTimeoutMs(), 30000));
        return new RestTemplate(requestFactory);
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value == null ? fallback : value;
    }
}
