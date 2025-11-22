package com.mudosa.musinsa.common.client;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Getter
@Builder
public class ClientRequest<T> {
    private String url;
    private HttpMethod method;
    private T body;
    private HttpHeaders headers;
    private Class<?> responseType;

    public static <T> ClientRequestBuilder<T> post(String url, Class<?> responseType) {
        return ClientRequest.<T>builder()
                .url(url)
                .method(HttpMethod.POST)
                .responseType(responseType);
    }

    public static <T> ClientRequestBuilder<T> get(String url, Class<?> responseType) {
        return ClientRequest.<T>builder()
                .url(url)
                .method(HttpMethod.GET)
                .responseType(responseType);
    }
}
