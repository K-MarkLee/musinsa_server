package com.mudosa.musinsa.settlement.batch.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

/* 배치 처리 관련 설정값 */
@Getter
@ConfigurationProperties(prefix = "batch.settlement")
public class BatchProperties {

    private final int chunkSize;
    private final int maxSkipCount;

    @ConstructorBinding
    public BatchProperties(
        @DefaultValue("10") int chunkSize,
        @DefaultValue("100") int maxSkipCount
    ) {
        this.chunkSize = chunkSize;
        this.maxSkipCount = maxSkipCount;
    }
}