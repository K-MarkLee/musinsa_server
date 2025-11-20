package com.mudosa.musinsa.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalApiException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String responseBody;

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
        this.responseBody = null;
    }

    public ExternalApiException(
            String message,
            HttpStatus httpStatus,
            String responseBody,
            Throwable cause
    ) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }
}
