package com.mudosa.musinsa.common.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HttpHeadersBuilder {
    private final BasicAuthStrategy basicAuthStrategy;

    public HttpHeadersBuilder(
            BasicAuthStrategy basicAuthStrategy
    ) {
        this.basicAuthStrategy = basicAuthStrategy;
    }

    // JSON 기본 헤더
    public HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    // Basic 인증 헤더
    public HttpHeaders basicAuth(String secretKey) {
        HttpHeaders headers = jsonHeaders();
        basicAuthStrategy.authenticate(headers, secretKey);
        return headers;
    }

}
