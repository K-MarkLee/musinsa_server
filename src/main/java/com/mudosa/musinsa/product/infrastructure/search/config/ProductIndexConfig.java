package com.mudosa.musinsa.product.infrastructure.search.config;

import com.mudosa.musinsa.product.infrastructure.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

/**
 * 상품 인덱스가 없을 때 최초에 생성해주는 설정.
 * settings.json / @Field 매핑 정보를 기반으로 생성한다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductIndexConfig {

    private final ElasticsearchOperations elasticsearchOperations;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexIfNotExists() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (indexOps.exists()) {
            log.info("Elasticsearch index 'product' already exists.");
            return;
        }

        boolean created = indexOps.create();
        if (created) {
            indexOps.putMapping(indexOps.createMapping());
            log.info("Elasticsearch index 'product' created with mapping/settings.");
        } else {
            log.warn("Failed to create Elasticsearch index 'product'.");
        }
    }
}
