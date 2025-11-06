package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.ArrayList;

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
}
