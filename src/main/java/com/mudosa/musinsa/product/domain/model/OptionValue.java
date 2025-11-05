package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "option_value")
public class OptionValue extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_value_id")
    private Long optionValueId;

    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;
    
    @Column(name = "option_value", nullable = false, length = 50)
    private String optionValue;
    
    @Builder
    public OptionValue(String optionName, String optionValue) {
        // 필수 파라미터를 확인해 무결성을 보장한다.
        if (optionName == null) {
            throw new IllegalArgumentException("옵션명은 필수입니다.");
        }
        if (optionValue == null || optionValue.trim().isEmpty()) {
            throw new IllegalArgumentException("옵션 값은 필수입니다.");
        }
        
        this.optionName = optionName;
        this.optionValue = optionValue;
    }
    
    // 현재 옵션 값이 비어 있지 않은지 확인한다.
    public boolean isValid() {
        return this.optionValue != null && !this.optionValue.trim().isEmpty();
    }
}