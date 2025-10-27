package com.mudosa.musinsa.common.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상태 코드 마스터 테이블
 * - 공통 참조 데이터 (ORDER, PAYMENT 등)
 */
@Entity
@Table(name = "status_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusCode extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_code_id")
    private Integer id;
    
    @Column(name = "status_code", nullable = false, length = 50)
    private String statusCode;
    
    @Column(name = "domain_type", nullable = false, length = 50)
    private String domainType;
    
    @Column(name = "description")
    private String description;
    
    /**
     * 상태 코드 생성
     */
    public static StatusCode create(String statusCode, String domainType, String description) {
        StatusCode entity = new StatusCode();
        entity.statusCode = statusCode;
        entity.domainType = domainType;
        entity.description = description;
        return entity;
    }
}
