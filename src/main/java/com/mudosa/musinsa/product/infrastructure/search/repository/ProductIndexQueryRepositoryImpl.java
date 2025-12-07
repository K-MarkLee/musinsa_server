package com.mudosa.musinsa.product.infrastructure.search.repository;

import com.mudosa.musinsa.product.infrastructure.cache.OptionValueCache;
import com.mudosa.musinsa.product.infrastructure.search.dto.ProductIndexDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 네이티브 프로젝션으로 옵션 단위 색인 데이터를 chunk 단위 조회한다.
 *
 * 쿼리는 DB 스키마에 맞게 수정 필요하다. (상품/옵션/옵션값/이미지/브랜드 조인)
 */
@Repository
@RequiredArgsConstructor
public class ProductIndexQueryRepositoryImpl implements ProductIndexQueryRepository {

    private final EntityManager em;
    private final OptionValueCache optionValueCache;

    @Override
    public List<ProductIndexDto> findChunk(Long lastOptionId, int pageSize) {
        // TODO: 스키마에 맞게 쿼리를 보완하세요.
        // 현재는 최소한의 형태로 옵션/상품/브랜드/카테고리/가격/썸네일/재고만 가져오는 예시입니다.
        String sql = """
            SELECT po.product_option_id,
                   p.product_id,
                   p.brand_id,
                   p.product_name,
                   p.brand_name      AS kr_brand_name,
                   b.name_en         AS en_brand_name,
                   p.category_path,
                   p.product_gender_type AS gender,
                   p.is_available,
                   p.default_price   AS default_price,
                   p.thumbnail_image AS thumbnail_url,
                   inv.stock_quantity AS stock_quantity
              FROM product_option po
              JOIN product p ON po.product_id = p.product_id
              JOIN brand b ON p.brand_id = b.brand_id
              LEFT JOIN inventory inv ON po.inventory_id = inv.inventory_id
             WHERE (:lastId IS NULL OR po.product_option_id > :lastId)
             ORDER BY po.product_option_id
             LIMIT :limit
            """;

        Query query = em.createNativeQuery(sql)
            .setParameter("lastId", lastOptionId)
            .setParameter("limit", pageSize);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<ProductIndexDto> result = new ArrayList<>(rows.size());

        for (Object[] row : rows) {
            Long productOptionId = toLong(row[0]);
            Long productId = toLong(row[1]);
            Long brandId = toLong(row[2]);
            String productName = (String) row[3];
            String krBrandName = (String) row[4];
            String enBrandName = (String) row[5];
            String categoryPath = (String) row[6];
            String gender = (String) row[7];
            Boolean isAvailable = toBoolean(row[8]);
            java.math.BigDecimal defaultPrice = toBigDecimal(row[9]);
            String thumbnailUrl = (String) row[10];
            Integer stockQuantity = toInteger(row[11]);
            Boolean hasStock = stockQuantity != null ? stockQuantity != 0 : null;

            result.add(ProductIndexDto.builder()
                .productOptionId(productOptionId)
                .productId(productId)
                .brandId(brandId)
                .productName(productName)
                .krBrandName(krBrandName)
                .enBrandName(enBrandName)
                .categoryPath(categoryPath)
                .gender(gender)
                .isAvailable(isAvailable)
                .defaultPrice(defaultPrice)
                .thumbnailUrl(thumbnailUrl)
                .hasStock(hasStock)
                .colorOptions(List.of())
                .sizeOptions(List.of())
                .build());
        }
        return mergeOptionValues(result);
    }

    // 옵션값을 별도 조회 후 dto에 병합한다.
    private List<ProductIndexDto> mergeOptionValues(List<ProductIndexDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }
        List<Long> optionIds = dtos.stream()
            .map(ProductIndexDto::getProductOptionId)
            .filter(id -> id != null)
            .toList();
        if (optionIds.isEmpty()) {
            return dtos;
        }

        Map<Long, List<Long>> optionIdToValueIds = fetchOptionValueIds(optionIds);
        if (optionIdToValueIds.isEmpty()) {
            return dtos;
        }

        Set<Long> allValueIds = optionIdToValueIds.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        Map<Long, OptionValueCache.Value> cachedValues = optionValueCache.getAll(allValueIds);

        Map<Long, List<String>> colorGrouped = new HashMap<>();
        Map<Long, List<String>> sizeGrouped = new HashMap<>();
        optionIdToValueIds.forEach((optionId, valueIds) -> {
            Set<String> colors = new LinkedHashSet<>();
            Set<String> sizes = new LinkedHashSet<>();
            for (Long valueId : valueIds) {
                OptionValueCache.Value cached = cachedValues.get(valueId);
                if (cached != null) {
                    if (isSizeId(valueId)) {
                        sizes.add(cached.optionValue());
                    } else if (isColorId(valueId)) {
                        colors.add(cached.optionValue());
                    }
                }
            }
            colorGrouped.put(optionId, new ArrayList<>(colors));
            sizeGrouped.put(optionId, new ArrayList<>(sizes));
        });

        List<ProductIndexDto> merged = new ArrayList<>(dtos.size());
        for (ProductIndexDto dto : dtos) {
            List<String> colorOptions = colorGrouped.getOrDefault(dto.getProductOptionId(), Collections.emptyList());
            List<String> sizeOptions = sizeGrouped.getOrDefault(dto.getProductOptionId(), Collections.emptyList());
            merged.add(ProductIndexDto.builder()
                .productOptionId(dto.getProductOptionId())
                .productId(dto.getProductId())
                .brandId(dto.getBrandId())
                .productName(dto.getProductName())
                .krBrandName(dto.getKrBrandName())
                .enBrandName(dto.getEnBrandName())
                .categoryPath(dto.getCategoryPath())
                .gender(dto.getGender())
                .isAvailable(dto.getIsAvailable())
                .defaultPrice(dto.getDefaultPrice())
                .thumbnailUrl(dto.getThumbnailUrl())
                .hasStock(dto.getHasStock())
                .colorOptions(colorOptions)
                .sizeOptions(sizeOptions)
                .build());
        }
        return merged;
    }

    // product_option_value 테이블에서 옵션값 ID를 조회해 옵션별로 그룹핑한다.
    private Map<Long, List<Long>> fetchOptionValueIds(List<Long> optionIds) {
        String sql = """
            SELECT pov.product_option_id, pov.option_value_id
              FROM product_option_value pov
             WHERE pov.product_option_id IN (:ids)
            """;
        Query query = em.createNativeQuery(sql)
            .setParameter("ids", optionIds);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        Map<Long, List<Long>> mapping = new HashMap<>();
        for (Object[] row : rows) {
            Long optionId = toLong(row[0]);
            Long valueId = toLong(row[1]);
            if (optionId == null || valueId == null) {
                continue;
            }
            mapping.computeIfAbsent(optionId, k -> new ArrayList<>()).add(valueId);
        }
        return mapping;
    }

    private boolean isSizeId(Long optionValueId) {
        return optionValueId != null && optionValueId >= 1 && optionValueId <= 8;
    }

    private boolean isColorId(Long optionValueId) {
        return optionValueId != null && optionValueId >= 9 && optionValueId <= 60;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        return Long.valueOf(o.toString());
    }

    private Integer toInteger(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        return Integer.valueOf(o.toString());
    }

    private java.math.BigDecimal toBigDecimal(Object o) {
        if (o == null) return null;
        if (o instanceof java.math.BigDecimal bd) return bd;
        if (o instanceof BigInteger bi) return new java.math.BigDecimal(bi);
        if (o instanceof Number n) return new java.math.BigDecimal(n.toString());
        return new java.math.BigDecimal(o.toString());
    }

    private Boolean toBoolean(Object o) {
        if (o == null) return null;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return Boolean.valueOf(o.toString());
    }
}
