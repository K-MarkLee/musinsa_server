package com.mudosa.musinsa.common.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthStrategy {
    public void authenticate(HttpHeaders headers, String secretKey) {
        String auth = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
    }
}
