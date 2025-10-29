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
@Table(name = "option_name")
public class OptionName extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_name_id")
    private Long optionNameId;

    @Column(name = "option_name", nullable = false, length = 50)
    private String optionName;

    @Builder
    public OptionName(String optionName) {
        // 엔티티 기본 무결성 검증
        if (optionName == null || optionName.trim().isEmpty()) {
            throw new IllegalArgumentException("옵션명은 필수입니다.");
        }

        this.optionName = optionName;
    }

    // 도메인 로직: 옵션 이름 유효성 확인
    public boolean isValid() {
        return this.optionName != null && !this.optionName.trim().isEmpty();
    }
}