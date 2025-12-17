package com.mudosa.musinsa.product.presentation.controller;

import com.mudosa.musinsa.product.infrastructure.search.service.ProductIndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ES 색인 실행용 임시 엔드포인트 (dev/관리자용).
 */
@RestController
@RequestMapping("/internal/search")
@RequiredArgsConstructor
public class ProductIndexController {

    private final ProductIndexingService productIndexingService;

    @PostMapping("/reindex")
    public ResponseEntity<Void> reindex() {
        productIndexingService.reindexAll();
        return ResponseEntity.accepted().build();
    }
}
