package com.mudosa.musinsa;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {BatchAutoConfiguration.class})
@EnableScheduling
@MapperScan(
    basePackages = {
        "com.mudosa.musinsa.settlement.domain.repository",
        "com.mudosa.musinsa.domain.order.mapper",
        "com.mudosa.musinsa.domain.payment.mapper"
    },
    annotationClass = Mapper.class
)
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
