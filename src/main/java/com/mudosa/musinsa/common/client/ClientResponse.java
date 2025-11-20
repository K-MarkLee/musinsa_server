package com.mudosa.musinsa.common.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public class ClientResponse<T> {
    private T body;
    private HttpStatusCode status;
    private HttpHeaders headers;

    public boolean isSuccess() {
        return status.is2xxSuccessful();
    }

}
