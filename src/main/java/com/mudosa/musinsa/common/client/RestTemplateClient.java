package com.mudosa.musinsa.common.client;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
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

import java.net.SocketTimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestTemplateClient {

    private final RestTemplate restTemplate;

    public <T> T executePayment(
            String url,
            HttpEntity<?> entity,
            Class<T> responseType,
            String orderId
    ) {
        try {
            ResponseEntity<T> response = restTemplate.postForEntity(url, entity, responseType);

            if (response.getBody() == null) {
                log.error("[Payment API] 응답 body가 null - orderNo: {}", orderId);
                throw new BusinessException(ErrorCode.PAYMENT_APPROVAL_FAILED);
            }

            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("[Payment API] HTTP 오류 - orderNo: {}, status: {}, body: {}",
                    orderId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalApiException(
                    "API 호출 실패",
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

        } catch (ResourceAccessException e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                log.error("[Payment API] 타임아웃 - orderNo: {}", orderId);
                throw new ExternalApiException(
                        "API 타임아웃",
                        HttpStatus.REQUEST_TIMEOUT,
                        "요청 시간이 초과되었습니다",
                        e
                );
            }
            log.error("[Payment API] 네트워크 오류 - orderNo: {}", orderId, e);
            throw new ExternalApiException("네트워크 오류", e);
        }
    }
}
