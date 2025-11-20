package com.mudosa.musinsa.common.client;

import com.mudosa.musinsa.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestTemplateApiClient implements ExternalApiClient{

    private final RestTemplate restTemplate;

    @Override
    public <T> ClientResponse<T> execute(ClientRequest<?> request) {
        try{
            HttpEntity<?> entity = new HttpEntity<>(
                    request.getBody(),
                    request.getHeaders()
            );

            ResponseEntity<T> response = (ResponseEntity<T>) restTemplate.exchange(
                    request.getUrl(),
                    request.getMethod(),
                    entity,
                    request.getResponseType()
            );

            return new ClientResponse<>(
                    response.getBody(),
                    response.getStatusCode(),
                    response.getHeaders()
            );
        } catch (HttpClientErrorException e) {
            throw new ExternalApiException(
                    "API 호출 실패",
                    (HttpStatus) e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

        } catch (HttpServerErrorException e) {
            throw new ExternalApiException(
                    "API 서버 오류",
                    (HttpStatus) e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("API 타임아웃 - url: {}", request.getUrl());
                throw new ExternalApiException(
                        "API 타임아웃",
                        HttpStatus.REQUEST_TIMEOUT,
                        "요청 시간이 초과되었습니다",
                        e
                );
            } else if (e.getCause() instanceof ConnectException) {
                log.error("API 연결 실패 - url: {}", request.getUrl());
                throw new ExternalApiException(
                        "API 연결 실패",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "서버에 연결할 수 없습니다",
                        e
                );
            }
            throw new ExternalApiException("API 호출 중 네트워크 오류", e);

        } catch (Exception e) {
            throw new ExternalApiException("API 호출 중 예상치 못한 오류", e);
        }
    }
}
