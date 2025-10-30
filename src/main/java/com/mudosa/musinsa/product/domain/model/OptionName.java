package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 옵션 이름을 관리하는 엔티티이다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "option_name")
public class OptionName extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_name_id")
    private Long optionNameId;
    
    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;
    
    // 옵션명을 생성하며 공백 여부를 검증한다.
    @Builder
    public OptionName(String optionName) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (optionName == null || optionName.trim().isEmpty()) {
            throw new IllegalArgumentException("옵션명은 필수입니다.");
        }
        
        this.optionName = optionName;
    }

    // 현재 옵션명이 비어 있지 않은지 확인한다.
    public boolean isValid() {
        return this.optionName != null && !this.optionName.trim().isEmpty();
    }
}