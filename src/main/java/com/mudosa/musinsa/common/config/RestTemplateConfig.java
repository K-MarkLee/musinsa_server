package com.mudosa.musinsa.common.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 설정
 */
@Configuration
public class RestTemplateConfig {

	/**
	 * RestTemplate Bean 생성
	 * 외부 API 호출 시 사용 (토스페이먼츠 등)
	 *
	 * @param builder RestTemplateBuilder
	 * @return RestTemplate
	 */
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.setConnectTimeout(Duration.ofSeconds(5)) // 연결 타임아웃 5초
				.setReadTimeout(Duration.ofSeconds(10)) // 읽기 타임아웃 10초
				.build();
	}
}
