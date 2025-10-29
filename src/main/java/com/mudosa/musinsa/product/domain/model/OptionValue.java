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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_name_id", nullable = false)
    private OptionName optionName;

    @Column(name = "option_value", nullable = false, length = 50)
    private String optionValue;

    @Builder
    public OptionValue(OptionName optionName, String optionValue) {
        // 엔티티 기본 무결성 검증
        if (optionName == null) {
            throw new IllegalArgumentException("옵션명은 필수입니다.");
        }
        if (optionValue == null || optionValue.trim().isEmpty()) {
            throw new IllegalArgumentException("옵션 값은 필수입니다.");
        }

        this.optionName = optionName;
        this.optionValue = optionValue;
    }

    // 도메인 로직: 옵션 값 유효성 확인
    public boolean isValid() {
        return this.optionValue != null && !this.optionValue.trim().isEmpty();
    }

    // 도메인 로직: 옵션 이름과 연관된 값인지 확인
    public boolean belongsTo(OptionName optionName) {
        return this.optionName != null && this.optionName.equals(optionName);
    }
}