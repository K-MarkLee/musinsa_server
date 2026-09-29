package com.mudosa.musinsa.product.application;

import com.mudosa.musinsa.product.application.dto.ProductSearchCondition;
import com.mudosa.musinsa.product.application.dto.CategoryTreeResponse;
import com.mudosa.musinsa.product.infrastructure.cache.OptionValueCache;
import com.mudosa.musinsa.product.domain.repository.*;
import com.mudosa.musinsa.product.infrastructure.cache.CategoryCache;
import com.mudosa.musinsa.product.infrastructure.search.repository.ProductIndexSearchQueryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ProductSearchTransactionTest {
    private final DataSourceTransactionManager transactions = new DataSourceTransactionManager(
        new DriverManagerDataSource("jdbc:h2:mem:product_tx_boundary", "sa", ""));

    @SuppressWarnings("unchecked")
    private <T> T transactionalProxy(T target) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
        return (T) factory.getProxy();
    }

    @Test
    void esDoesNotUseDbTransactionWhileDbReadsKeepReadOnlyTransactions() {
        var es = mock(ProductIndexSearchQueryRepository.class);
        var cache = mock(CategoryCache.class);
        var products = mock(ProductRepository.class);
        var service = transactionalProxy(new ProductQueryService(
            mock(CategoryRepository.class), cache, products, mock(OptionValueCache.class), es));
        when(es.searchByKeywordWithFilters(any(), anyList(), anyInt())).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return new ProductIndexSearchQueryRepository.SearchResult(List.of(), false, 0L);
        });
        var condition = ProductSearchCondition.builder().keyword("스커트").limit(10).build();
        service.searchProducts(condition);
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            service.searchProducts(condition);
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        });
        verify(es, times(2)).searchByKeywordWithFilters(any(), anyList(), anyInt());

        when(cache.getTree()).thenAnswer(invocation -> {
            assertReadOnlyTransaction();
            return CategoryTreeResponse.builder().categories(List.of()).build();
        });
        service.getCategoryTree();
        var stop = new IllegalStateException("stop after transaction assertion");
        when(products.findDetailById(1L)).thenAnswer(invocation -> {
            assertReadOnlyTransaction();
            throw stop;
        });
        assertThatThrownBy(() -> service.getProductDetail(1L)).isSameAs(stop);

        // Invoke the real repository method through Spring's transaction interceptor.
        var queries = mock(JPAQueryFactory.class, invocation -> {
            assertReadOnlyTransaction();
            throw stop;
        });
        var repository = transactionalProxy(new ProductRepositoryImpl(queries));
        assertThatThrownBy(() -> repository.findAllByFiltersWithCursor(null, null, null, null, null, 25))
            .isSameAs(stop);
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }

    private void assertReadOnlyTransaction() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly()).isTrue();
    }
}
