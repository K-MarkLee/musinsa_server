package com.mudosa.musinsa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.redisson.api.RedissonClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import com.mudosa.musinsa.product.application.ProductQueryService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context.getBeansOfType(S3Client.class)).isEmpty();
        assertThat(context.getBeansOfType(S3AsyncClient.class)).isEmpty();
        assertThat(context.getBeansOfType(RedissonClient.class)).isEmpty();
        assertThat(context.getBeansOfType(RedisConnectionFactory.class)).hasSize(1);
        assertThat(context.getBean(ProductQueryService.class)).isNotNull();
    }

}
