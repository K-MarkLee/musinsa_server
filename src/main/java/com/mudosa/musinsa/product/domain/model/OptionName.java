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
        this.optionName = optionName;
    }
}