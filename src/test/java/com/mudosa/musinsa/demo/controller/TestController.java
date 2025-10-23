package com.mudosa.musinsa.demo.controller;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class TestController {
    @Test
    public void test(){
        log.info("Hello World!!!");
    }
}
