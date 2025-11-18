package com.mudosa.musinsa;

import com.mudosa.musinsa.config.JpaConfig;
import com.mudosa.musinsa.config.QuerydslConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableAutoConfiguration
@EntityScan(basePackages = "com.mudosa.musinsa")
@Import({JpaConfig.class, QuerydslConfig.class})
public class TestJpaConfig {
    // 테스트 전용 구성입니다. @SpringBootConfiguration을 사용하면 테스트 실행 시
    // 메인 애플리케이션(예: ServerApplication)과 충돌하여 "Found multiple @SpringBootConfiguration" 예외가 발생합니다.
    // 따라서 여기서는 일반 @Configuration을 사용해 테스트 컨텍스트에서만 로드되도록 합니다.
}
