package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.common.domain.model.BaseEntity;
import com.mudosa.musinsa.product.domain.vo.OptionValueVo;
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
    @JoinColumn(name = "option_name_id", nullable = false, foreignKey = @ForeignKey(name = "fk_optionval_optnam"))
    private OptionName optionName;
    
    @Column(name = "option_value", nullable = false, length = 50)
    private OptionValueVo optionValue;
    
    @Builder
    public OptionValue(OptionName optionName, OptionValueVo optionValue) {
        this.optionName = optionName;
        this.optionValue = optionValue;
    }
    
    // 도메인 로직: 수정
    public void modify(OptionValueVo optionValue) {
        if (optionValue != null) this.optionValue = optionValue;
    }
}