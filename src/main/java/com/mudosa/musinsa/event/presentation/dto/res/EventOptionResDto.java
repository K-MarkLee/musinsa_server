package com.mudosa.musinsa.event.presentation.dto.res;

import com.mudosa.musinsa.event.model.EventOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@NoArgsConstructor


public class EventOptionResDto {

    private Long optionId;
    private Long productOptionId;
    private String productName;
    private String thumbnailUrl;   // 대표 이미지 URL
    private BigDecimal eventPrice; // 이벤트 가격
    private Integer eventStock;

    // private String description; 필요한가?

    public static EventOptionResDto from(EventOption eo, String productName, String thumbnailUrl) {
        //[static]: 클래스 레벨에 속한다. 인스턴스(객체)를 만들지 않아도 클래스 이름으로 직접 호출 가능
        return new EventOptionResDto(
                eo.getId(),
                eo.getProductOption().getId(),
                productName,
                thumbnailUrl,
                eo.getEventPrice(),
                eo.getEventStock()
        );
    }





}
