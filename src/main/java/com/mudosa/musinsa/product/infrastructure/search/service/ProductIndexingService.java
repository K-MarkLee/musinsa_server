package com.mudosa.musinsa.product.infrastructure.search.service;

import com.mudosa.musinsa.product.infrastructure.search.document.ProductDocument;
import com.mudosa.musinsa.product.infrastructure.search.dto.ProductIndexDto;
import com.mudosa.musinsa.product.infrastructure.search.mapper.ProductDocumentMapper;
import com.mudosa.musinsa.product.infrastructure.search.repository.ProductIndexQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DB 데이터를 읽어 ES에 색인하는 서비스. 대량 색인을 위해 chunk + bulk를 사용한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductIndexingService {

    private static final int DEFAULT_PAGE_SIZE = 1000;
    private static final IndexCoordinates INDEX = IndexCoordinates.of("product");

    private final ProductIndexQueryRepository productIndexQueryRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    /**
     * 전체 상품 옵션을 읽어 색인한다. (dev/임시용)
     */
    @Transactional(readOnly = true)
    public void reindexAll() {
        reindexAll(DEFAULT_PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public void reindexAll(int pageSize) {
        Long cursor = null;
        int batch = 0;
        while (true) {
            List<ProductIndexDto> chunk = productIndexQueryRepository.findChunk(cursor, pageSize);
            if (chunk.isEmpty()) {
                break;
            }
            List<ProductDocument> documents = chunk.stream()
                .map(ProductDocumentMapper::toDocument)
                .collect(Collectors.toList());

            bulkIndex(documents);

            cursor = chunk.get(chunk.size() - 1).getProductOptionId();
            batch++;
            log.info("Indexed batch {} (last optionId={})", batch, cursor);

            if (chunk.size() < pageSize) {
                break;
            }
        }
        // 색인 완료 후 refresh
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        indexOps.refresh();
        log.info("Reindexing completed");
    }

    private void bulkIndex(List<ProductDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }
        List<IndexQuery> queries = new ArrayList<>(documents.size());
        for (ProductDocument doc : documents) {
            queries.add(new IndexQueryBuilder()
                .withId(doc.getProductOptionId() != null ? doc.getProductOptionId().toString() : null)
                .withObject(doc)
                .build());
        }
        List<IndexedObjectInformation> results = elasticsearchOperations.bulkIndex(queries, INDEX);
        if (results == null || results.isEmpty()) {
            log.warn("Bulk index returned empty result for {} documents", documents.size());
        }
    }
}
