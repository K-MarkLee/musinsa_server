package com.mudosa.musinsa.order.application;

import com.mudosa.musinsa.ServerApplication;
import com.mudosa.musinsa.order.domain.model.Orders;
import com.mudosa.musinsa.order.domain.model.QOrders;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderServiceTest {
    @Autowired
    private EntityManager em;

    @Test
    void contextLoads(){
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrders qOrders = new QOrders("o");

        Orders result = query
                .selectFrom(qOrders)
                .fetchOne();

        assertThat(result).isNull();
    }
}