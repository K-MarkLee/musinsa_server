package com.mudosa.musinsa.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ExternalApiException extends RuntimeException {
    private final HttpStatusCode httpStatus;
    private final String responseBody;

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.responseBody = null;
    }

    public ExternalApiException(
            String message,
            HttpStatusCode httpStatusCode,
            String responseBody,
            Throwable cause
    ) {
        super(message, cause);
        this.httpStatus = httpStatusCode;
        this.responseBody = responseBody;
    }
}
