package com.mudosa.musinsa.product.domain.repository;

import com.mudosa.musinsa.product.domain.model.Product;
import com.mudosa.musinsa.product.domain.model.ProductGenderType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

// 상품 상세 조회와 조건 검색을 담당하는 커스텀 구현체로 연관 로딩 전략을 직접 제어한다.
@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // 상품 ID로 상세 정보를 조회하고 필요한 연관을 순차적으로 초기화한다.
    @Override
    public Optional<Product> findDetailById(Long productId) {
        TypedQuery<Product> query = entityManager.createQuery(
            "select p from Product p " +
                "join fetch p.brand " +
                "where p.productId = :productId",
            Product.class
        );
        query.setParameter("productId", productId);

        List<Product> results = query.getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }

        Product product = results.get(0);
        initializeCollections(product);
        return Optional.of(product);
    }

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

        cq.select(product).distinct(true);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(cq).getResultList();
    }

    // 상세 조회 시 필요한 컬렉션과 연관 엔티티를 Hibernate.initialize로 강제 로딩한다.
    private void initializeCollections(Product product) {
        Hibernate.initialize(product.getImages());
        Hibernate.initialize(product.getProductCategories());
        Hibernate.initialize(product.getProductOptions());

        product.getProductCategories().forEach(mapping -> {
            if (mapping != null) {
                Hibernate.initialize(mapping.getCategory());
            }
        });

        product.getProductOptions().forEach(option -> {
            Hibernate.initialize(option.getInventory());
            Hibernate.initialize(option.getProductOptionValues());
            option.getProductOptionValues().forEach(mapping -> {
                if (mapping != null) {
                    Hibernate.initialize(mapping.getOptionValue());
                    if (mapping.getOptionValue() != null) {
                        Hibernate.initialize(mapping.getOptionValue().getOptionName());
                    }
                }
            });
        });
    }
}
