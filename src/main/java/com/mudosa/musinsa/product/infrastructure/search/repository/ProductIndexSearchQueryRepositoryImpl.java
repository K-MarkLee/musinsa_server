package com.mudosa.musinsa.product.infrastructure.search.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.application.dto.ProductSearchResponse;
import com.mudosa.musinsa.product.infrastructure.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch.core.search.FieldCollapse;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductIndexSearchQueryRepositoryImpl implements ProductIndexSearchQueryRepository {

    private static final String FIELD_PRODUCT_ID = "productId";
    private static final String FIELD_PRODUCT_NAME = "productName";
    private static final String FIELD_KR_BRAND_TEXT = "krBrandName.text";
    private static final String FIELD_EN_BRAND_TEXT = "enBrandName";
    private static final String FIELD_CATEGORY_PATH = "categoryPath";
    private static final String FIELD_CATEGORY_TEXT = "categoryPath.text";
    private static final String FIELD_COLOR_TEXT = "colorOptions.text";
    private static final String FIELD_SIZE_TEXT = "sizeOptions.text";
    private static final String FIELD_PRICE = "defaultPrice";
    private static final Set<String> ROOT_CATEGORIES = Set.of(
        "상의", "아우터", "바지", "원피스", "스커트", "가방", "패션소품", "속옷", "홈웨어"
    );

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchResult searchByKeywordWithFilters(ProductSearchCondition condition, List<String> tokens, int page) {
        Query base = baseFilter(condition);
        String rawQuery = condition.getKeyword();
        List<String> safeTokens = tokens != null ? tokens : Collections.emptyList();

        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (base.bool() != null) {
            bool.filter(base.bool().filter());
        }

        for (String token : safeTokens) {
            if (token == null || token.isBlank()) {
                continue;
            }
            bool.must(perTokenShould(token));
        }

        if (rawQuery != null && !rawQuery.isBlank()) {
            bool.should(MatchQuery.of(m -> m.field(FIELD_PRODUCT_NAME).query(rawQuery).boost(2f))._toQuery());
            bool.should(MatchQuery.of(m -> m.field(FIELD_CATEGORY_TEXT).query(rawQuery).boost(3f))._toQuery());
            bool.should(MatchQuery.of(m -> m.field(FIELD_COLOR_TEXT).query(rawQuery).boost(3f))._toQuery());
            bool.should(MatchQuery.of(m -> m.field(FIELD_SIZE_TEXT).query(rawQuery).boost(1f))._toQuery());
            bool.should(MatchQuery.of(m -> m.field(FIELD_KR_BRAND_TEXT).query(rawQuery).boost(1f))._toQuery());
            bool.should(MatchQuery.of(m -> m.field(FIELD_EN_BRAND_TEXT).query(rawQuery).boost(1f))._toQuery());
        }

        Query query = bool.build()._toQuery();
        return execute(condition, query);
    }

    @Override
    public SearchResult findAllByFiltersWithCursor(ProductSearchCondition condition) {
        Query base = baseFilter(condition);
        BoolQuery.Builder bool = new BoolQuery.Builder();
        if (base.bool() != null) {
            bool.filter(base.bool().filter());
        }
        bool.must(q -> q.matchAll(m -> m));
        return execute(condition, bool.build()._toQuery());
    }

    // 기본 쿼리 필터 (필터링 시 사용)
    private Query baseFilter(ProductSearchCondition condition) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        bool.filter(TermQuery.of(t -> t.field("hasStock").value(true))._toQuery());
        bool.filter(TermQuery.of(t -> t.field("isAvailable").value(true))._toQuery());

        if (condition.getCategoryPaths() != null && !condition.getCategoryPaths().isEmpty()) {
            // 부모 카테고리 단일값이면 prefix로 하위까지 포함, 그 외에는 terms로 exact 매칭
            if (condition.getCategoryPaths().size() == 1 && ROOT_CATEGORIES.contains(condition.getCategoryPaths().get(0))) {
                String root = condition.getCategoryPaths().get(0);
                bool.filter(PrefixQuery.of(p -> p.field(FIELD_CATEGORY_PATH).value(root))._toQuery());
            } else {
                List<FieldValue> values = condition.getCategoryPaths().stream()
                    .filter(Objects::nonNull)
                    .map(FieldValue::of)
                    .toList();
                if (!values.isEmpty()) {
                    bool.filter(TermsQuery.of(t -> t.field(FIELD_CATEGORY_PATH).terms(TermsQueryField.of(f -> f.value(values))))._toQuery());
                }
            }
        }

        if (condition.getBrandId() != null) {
            bool.filter(TermQuery.of(t -> t.field("brandId").value(condition.getBrandId()))._toQuery());
        }
        if (condition.getGender() != null) {
            bool.filter(TermQuery.of(t -> t.field("gender").value(condition.getGender().name()))._toQuery());
        }

        // 가격 범위 필터는 condition 확장 시 여기에 추가
        return bool.build()._toQuery();
    }

    // 토큰별 should 쿼리 생성
    private Query perTokenShould(String token) {
        return BoolQuery.of(b -> b
            .should(MatchQuery.of(m -> m.field(FIELD_PRODUCT_NAME).query(token))._toQuery())
            .should(MatchQuery.of(m -> m.field(FIELD_CATEGORY_TEXT).query(token))._toQuery())
            .should(MatchQuery.of(m -> m.field(FIELD_COLOR_TEXT).query(token))._toQuery())
            .should(MatchQuery.of(m -> m.field(FIELD_SIZE_TEXT).query(token))._toQuery())
            .should(MatchQuery.of(m -> m.field(FIELD_KR_BRAND_TEXT).query(token))._toQuery())
            .should(MatchQuery.of(m -> m.field(FIELD_EN_BRAND_TEXT).query(token))._toQuery())
            .minimumShouldMatch("1")
        )._toQuery();
    }

    // elastic으로 전송해서 조건 실행
    private SearchResult execute(ProductSearchCondition condition, Query query) {
        List<SortOptions> sorts = buildSorts(condition.getPriceSort());
        int limit = condition.getLimit();
        int page = parsePage(condition.getCursor());
        boolean isScoreSort = condition.getPriceSort() == null;

        NativeQueryBuilder builder = NativeQuery.builder()
            .withQuery(query)
            .withSort(sorts)
            .withPageable(PageRequest.of(page, limit))
            // 가격 정렬일 땐 점수 계산을 생략해 CPU 부담을 줄인다
            .withTrackScores(isScoreSort)
            .withFieldCollapse(FieldCollapse.of(c -> c.field(FIELD_PRODUCT_ID)))
            .withSourceFilter(new FetchSourceFilterBuilder()
                .withIncludes(
                    "productOptionId",
                    "productId",
                    "brandId",
                    "krBrandName",
                    "productName",
                    "defaultPrice",
                    "thumbnailUrl"
                )
                .build())
            .withTrackTotalHits(false);

        NativeQuery nativeQuery = builder.build();
        return executeQuery(nativeQuery, limit);
    }

    // 쿼리 실행 및 결과 매핑
    private SearchResult executeQuery(NativeQuery query, int limit) {
        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);

        List<ProductSearchResponse.ProductSummary> products = hits.getSearchHits().stream()
            .map(hit -> toSummary(hit.getContent()))
            .collect(Collectors.toList());

        boolean hasNext = products.size() >= limit;
        Long total = hits.getTotalHits();

        return new SearchResult(products, hasNext, total);
    }

    // 정렬 옵션 빌드
    private List<SortOptions> buildSorts(ProductSearchCondition.PriceSort sort) {
        List<SortOptions> sorts = new ArrayList<>();
        if (sort == ProductSearchCondition.PriceSort.LOWEST) {
            sorts.add(SortOptions.of(s -> s.field(f -> f.field(FIELD_PRICE).order(SortOrder.Asc))));
            sorts.add(SortOptions.of(s -> s.field(f -> f.field(FIELD_PRODUCT_ID).order(SortOrder.Asc))));
        } else if (sort == ProductSearchCondition.PriceSort.HIGHEST) {
            sorts.add(SortOptions.of(s -> s.field(f -> f.field(FIELD_PRICE).order(SortOrder.Desc))));
            sorts.add(SortOptions.of(s -> s.field(f -> f.field(FIELD_PRODUCT_ID).order(SortOrder.Desc))));
        } else {
            sorts.add(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));
            sorts.add(SortOptions.of(s -> s.field(f -> f.field(FIELD_PRODUCT_ID).order(SortOrder.Desc))));
        }
        return sorts;
    }

    private int parsePage(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            int page = Integer.parseInt(cursor);
            return page >= 0 ? page : 0;
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // ProductDocument를 ProductSummary로 매핑
    private ProductSearchResponse.ProductSummary toSummary(ProductDocument doc) {
        BigDecimal price = doc.getDefaultPrice() != null ? BigDecimal.valueOf(doc.getDefaultPrice()) : null;
        return ProductSearchResponse.ProductSummary.builder()
            .productOptionId(doc.getProductOptionId())
            .productId(doc.getProductId())
            .brandId(doc.getBrandId())
            .brandName(doc.getKrBrandName())
            .productName(doc.getProductName())
            .productInfo(null)
            .productGenderType(null)
            .isAvailable(null)
            .hasStock(null)
            .lowestPrice(price)
            .thumbnailUrl(doc.getThumbnailUrl())
            .categoryPath(null)
            .build();
    }
}
