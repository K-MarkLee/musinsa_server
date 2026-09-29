package com.mudosa.musinsa;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {BatchAutoConfiguration.class},
    excludeName = "org.redisson.spring.starter.RedissonAutoConfiguration")
// 상품 조회 실행에 불필요한 외부 연동과 그 의존 빈을 제외한다. 도메인 모델·저장소는 유지한다.
@ComponentScan(basePackages = "com.mudosa.musinsa", excludeFilters = {
    @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
    @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = {
        "com\\.mudosa\\.musinsa\\.config\\.websocket\\..*",
        "com\\.mudosa\\.musinsa\\.config\\.(S3Config|FileStoreConfig)",
        "com\\.mudosa\\.musinsa\\.chat\\.(broker|controller|event|facade|file|service)\\..*",
        "com\\.mudosa\\.musinsa\\.brand\\.domain\\.(controller|service)\\..*",
        "com\\.mudosa\\.musinsa\\.coupon\\.(service|presentation)\\..*",
        "com\\.mudosa\\.musinsa\\.event\\.service\\.EventCouponService",
        "com\\.mudosa\\.musinsa\\.event\\.presentation\\.controller\\.EventController",
        "com\\.mudosa\\.musinsa\\.notification\\.controller\\.NotificationController",
        "com\\.mudosa\\.musinsa\\.notification\\.service\\.(NotificationService|FcmService)",
        "com\\.mudosa\\.musinsa\\.notification\\.event\\.NotificationEventListener"
    })
})
// 상품 조회 측정 중 스케줄러 비활성화.
// @EnableScheduling
@MapperScan(
    basePackages = {
        "com.mudosa.musinsa.settlement.domain.repository",
    },
    annotationClass = Mapper.class
)
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableCaching
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
