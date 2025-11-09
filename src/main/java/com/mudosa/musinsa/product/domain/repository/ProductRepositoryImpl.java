package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

// 상품 검색 조건을 조합해 목록 결과를 반환하는 커스텀 구현체.
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // 검색 조건을 Criteria API로 조합해 상품 목록을 조회한다.
    @Override
    public List<Product> findAllByFilters(List<String> categoryPaths,
                                          ProductGenderType gender,
                                          String keyword,
                                          Long brandId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (gender != null) {
            Expression<ProductGenderType> genderPath = product.get("productGenderType");
            predicates.add(cb.equal(genderPath, gender));
        }

        if (categoryPaths != null && !categoryPaths.isEmpty()) {
            Expression<String> categoryPathExpression = product.get("categoryPath");
            List<Predicate> categoryPredicates = new ArrayList<>();
            for (String path : categoryPaths) {
                Predicate exactMatch = cb.equal(categoryPathExpression, path);
                Predicate childMatch = cb.like(categoryPathExpression, path + "/%");
                categoryPredicates.add(cb.or(exactMatch, childMatch));
            }
            predicates.add(cb.or(categoryPredicates.toArray(new Predicate[0])));
        }

        // TODO: 검색 확장시 분리.
        if (keyword != null && !keyword.isBlank()) {
            String lowered = "%" + keyword.toLowerCase() + "%";
            Expression<String> namePath = cb.lower(product.get("productName"));
            Expression<String> infoPath = cb.lower(product.get("productInfo"));
            Expression<String> brandNamePath = cb.lower(product.get("brandName"));
            Expression<String> categoryPathExpr = cb.lower(product.get("categoryPath"));

            Predicate nameLike = cb.like(namePath, lowered);
            Predicate infoLike = cb.like(infoPath, lowered);
            Predicate brandLike = cb.like(brandNamePath, lowered);
            Predicate categoryLike = cb.like(categoryPathExpr, lowered);

            predicates.add(cb.or(nameLike, infoLike, brandLike, categoryLike));
        }

        if (brandId != null) {
            predicates.add(cb.equal(product.get("brand").get("brandId"), brandId));
        }

        // 항상 판매 가능 상품만 조회한다.
        predicates.add(cb.isTrue(product.get("isAvailable")));

        cq.select(product).distinct(true);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(cq).getResultList();
    }

    // 검색 조건을 Criteria API로 조합해 상품 목록을 데이터베이스 레벨에서 필터링, 정렬, 페이징하여 조회한다.
    @Override
    public Page<Product> findAllByFiltersWithPagination(List<String> categoryPaths,
                                                      ProductGenderType gender,
                                                      String keyword,
                                                      Long brandId,
                                                      ProductSearchCondition.PriceSort priceSort,
                                                      Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> product = cq.from(Product.class);

        List<Predicate> predicates = buildPredicates(cb, product, categoryPaths, gender, keyword, brandId);

        cq.select(product).distinct(true);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // 정렬 조건 적용
        applySorting(cb, cq, product, priceSort, pageable);

        // 페이징 적용
        int totalElements = countTotalElements(cb, categoryPaths, gender, keyword, brandId);
        
        jakarta.persistence.TypedQuery<Product> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Product> content = query.getResultList();
        return new PageImpl<>(content, pageable, totalElements);
    }

    // 공통 검색 조건 빌드
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Product> product,
                                         List<String> categoryPaths, ProductGenderType gender,
                                         String keyword, Long brandId) {
        List<Predicate> predicates = new ArrayList<>();

        if (gender != null) {
            Expression<ProductGenderType> genderPath = product.get("productGenderType");
            predicates.add(cb.equal(genderPath, gender));
        }

        if (categoryPaths != null && !categoryPaths.isEmpty()) {
            Expression<String> categoryPathExpression = product.get("categoryPath");
            List<Predicate> categoryPredicates = new ArrayList<>();
            for (String path : categoryPaths) {
                Predicate exactMatch = cb.equal(categoryPathExpression, path);
                Predicate childMatch = cb.like(categoryPathExpression, path + "/%");
                categoryPredicates.add(cb.or(exactMatch, childMatch));
            }
            predicates.add(cb.or(categoryPredicates.toArray(new Predicate[0])));
        }

        if (keyword != null && !keyword.isBlank()) {
            String lowered = "%" + keyword.toLowerCase() + "%";
            Expression<String> namePath = cb.lower(product.get("productName"));
            Expression<String> infoPath = cb.lower(product.get("productInfo"));
            Expression<String> brandNamePath = cb.lower(product.get("brandName"));
            Expression<String> categoryPathExpr = cb.lower(product.get("categoryPath"));

            Predicate nameLike = cb.like(namePath, lowered);
            Predicate infoLike = cb.like(infoPath, lowered);
            Predicate brandLike = cb.like(brandNamePath, lowered);
            Predicate categoryLike = cb.like(categoryPathExpr, lowered);

            predicates.add(cb.or(nameLike, infoLike, brandLike, categoryLike));
        }

        if (brandId != null) {
            predicates.add(cb.equal(product.get("brand").get("brandId"), brandId));
        }

        // 항상 판매 가능 상품만 조회한다.
        predicates.add(cb.isTrue(product.get("isAvailable")));

        return predicates;
    }

    // 정렬 조건 적용
    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Product> cq, Root<Product> product,
                            ProductSearchCondition.PriceSort priceSort, Pageable pageable) {
        List<Order> orders = new ArrayList<>();

        // 가격 정렬이 있는 경우 가장 먼저 적용
        if (priceSort != null) {
            // 최저가 계산을 위한 서브쿼리
            Subquery<java.math.BigDecimal> priceSubquery = cq.subquery(java.math.BigDecimal.class);
            Root<Product> subProduct = priceSubquery.correlate(product);
            jakarta.persistence.criteria.Join<Product, ?> options = subProduct.join("options");
            
            priceSubquery.select(cb.min(options.get("productPrice")))
                .where(cb.equal(subProduct.get("productId"), product.get("productId")));

            Expression<java.math.BigDecimal> lowestPriceExpr = priceSubquery.getSelection();
            
            if (priceSort == ProductSearchCondition.PriceSort.LOWEST) {
                orders.add(cb.asc(lowestPriceExpr));
            } else {
                orders.add(cb.desc(lowestPriceExpr));
            }
        }

        // Pageable에 포함된 정렬 조건 추가 (가격 정렬이 없는 경우에만)
        if (priceSort == null && pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                Expression<?> expression = product.get(order.getProperty());
                orders.add(order.isAscending() ? cb.asc(expression) : cb.desc(expression));
            });
        }

        // 기본 정렬 (상품 ID로 정렬하여 결과 일관성 보장)
        if (orders.isEmpty()) {
            orders.add(cb.asc(product.get("productId")));
        }

        cq.orderBy(orders);
    }

    // 전체 개수 계산
    private int countTotalElements(CriteriaBuilder cb, List<String> categoryPaths,
                                 ProductGenderType gender, String keyword, Long brandId) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Product> countRoot = countQuery.from(Product.class);
        
        List<Predicate> predicates = buildPredicates(cb, countRoot, categoryPaths, gender, keyword, brandId);
        
        countQuery.select(cb.countDistinct(countRoot));
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult().intValue();
    }
}
