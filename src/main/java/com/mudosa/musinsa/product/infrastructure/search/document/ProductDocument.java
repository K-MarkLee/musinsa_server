package com.mudosa.musinsa.product.infrastructure.search.document;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상품 옵션 단위로 색인되는 검색 도큐먼트.
 * 목록 표시용 필드와 검색/필터용 필드를 함께 둔다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "product")
@Setting(settingPath = "/elasticsearch/settings.json")
public class ProductDocument {

    // 식별자: 옵션 PK를 _id로 사용한다.
    @Id
    private Long productOptionId;

    // 상품 묶음 식별자
    @Field(type = FieldType.Long)
    private Long productId;

    // 브랜드 식별자 (필터/표시용)
    @Field(type = FieldType.Long)
    private Long brandId;

    // 표시 + 검색용 (text/keyword 멀티필드)
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_default", searchAnalyzer = "nori_default"),
            otherFields = @InnerField(suffix = "auto_complete", type = FieldType.Search_As_You_Type, analyzer = "nori_autocomplete"))
    private String productName;

    // 브랜드명 검색/표시
    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "text", type = FieldType.Text, analyzer = "nori_default"))
    private String krBrandName;

    // 영문 브랜드명 검색/표시
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = @InnerField(suffix = "keyword", type = FieldType.Keyword))
    private String enBrandName;

    // 필터용 카테고리 경로 (keyword), 검색용 텍스트 서브 필드
    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "text", type = FieldType.Text, analyzer = "nori_default"))
    private String categoryPath;

    // 옵션 값(색상/사이즈) - 검색용 text + 필터용 keyword 멀티필드
    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "text", type = FieldType.Text, analyzer = "nori_color"))
    private List<String> colorOptions;

    @MultiField(
            mainField = @Field(type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "text", type = FieldType.Text, analyzer = "nori_size"))
    private List<String> sizeOptions;


    // 가격(정렬/커서용) - 옵션가 또는 기본가
    // ES에서는 소수점 없는 정수 금액(예: 원 단위)으로 색인한다.
    @Field(type = FieldType.Long)
    private Long defaultPrice;

    // 썸네일 URL
    @Field(type = FieldType.Keyword)
    private String thumbnailUrl;

    // 노출/재고 상태 (기본값 1)
    @Field(type = FieldType.Boolean)
    private Boolean isAvailable;

    @Field(type = FieldType.Boolean)
    private Boolean hasStock;

    // 성별 필터
    @Field(type = FieldType.Keyword)
    private String gender;

}
