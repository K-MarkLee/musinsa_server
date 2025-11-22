package com.mudosa.musinsa.common.client;

public interface ExternalApiClient {
    <T> ClientResponse<T> execute(ClientRequest<?> request);
}
