package com.mudosa.musinsa.config;

import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Profile("dev")
public class ElasticsearchRestClientConfig implements RestClientBuilderCustomizer {

    @Override
    public void customize(RestClientBuilder builder) {
        // Spring Boot가 구성한 인증 및 타임아웃 설정을 유지한다.
    }

    @Override
    public void customize(HttpAsyncClientBuilder builder) {
        // 로컬 검색 측정 기준값. 증설만으로 ES 처리량 한계가 해소되지는 않았다.
        builder.setMaxConnTotal(150).setMaxConnPerRoute(150);
    }
}
