package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 옵션 값을 관리하며 옵션명과의 관계를 유지하는 엔티티이다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "option_value")
public class OptionValue extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_value_id")
    private Long optionValueId;
    
    @Column(name = "option_name", nullable = false)
    private String optionName;
    
    @Column(name = "option_value", nullable = false, length = 50)
    private String optionValue;

    // 외부 노출 생성 메서드 + 필수 값 검증
    public static OptionValue create(String optionName, String optionValue) {
        if (optionName == null || optionName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.OPTION_NAME_REQUIRED);
        }
        if (optionValue == null || optionValue.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.OPTION_VALUE_REQUIRED);
        }
        return new OptionValue(optionName, optionValue);
    }
    
    @Builder
    private OptionValue(String optionName, String optionValue) {
        this.optionName = optionName;
        this.optionValue = optionValue;
    }
}