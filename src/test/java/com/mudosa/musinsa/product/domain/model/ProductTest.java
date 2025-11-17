package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class ProductTest {

	@Nested
	@DisplayName("상품 생성")
	class 상품_생성 {

		@Test
		@DisplayName("필수 정보로 생성하면 기본 상태가 초기화된다")
		void buildProduct_initializesState() {
			// given
			Brand brand = Brand.create("신상", "SINSANG", BigDecimal.ONE);
			Image thumbnail = Image.create("https://example.com/thumbnail.jpg", true);

			// when
			Product product = Product.builder()
				.brand(brand)
				.productName("테스트 상품")
				.productInfo("상세 설명")
				.productGenderType(ProductGenderType.MEN)
				.brandName("신상")
				.categoryPath("상의/티셔츠")
				.isAvailable(true)
				.images(List.of(thumbnail))
				.build();

			// then
			assertThat(product.getBrand()).isEqualTo(brand);
			assertThat(product.getProductName()).isEqualTo("테스트 상품");
			assertThat(product.getProductInfo()).isEqualTo("상세 설명");
			assertThat(product.getProductGenderType()).isEqualTo(ProductGenderType.MEN);
			assertThat(product.getBrandName()).isEqualTo("신상");
			assertThat(product.getCategoryPath()).isEqualTo("상의/티셔츠");
			assertThat(product.getIsAvailable()).isTrue();
			assertThat(product.getImages()).hasSize(1)
				.allSatisfy(image -> assertThat(image.getProduct()).isSameAs(product));
			assertThat(product.getProductOptions()).isEmpty();
		}

		@Test
		@DisplayName("브랜드 없이 생성하면 예외가 발생한다")
		void buildProduct_withoutBrand_throwsIllegalArgumentException() {
			// given
			Image thumbnail = Image.create("https://example.com/thumb.jpg", true);

			// when & then
			assertThatThrownBy(() -> Product.builder()
				.brand(null)
				.productName("상품")
				.productInfo("설명")
				.productGenderType(ProductGenderType.ALL)
				.brandName("브랜드")
				.categoryPath("카테고리")
				.images(List.of(thumbnail))
				.build())
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("브랜드는 필수입니다.");
		}
        
	}

	@Nested
	@DisplayName("연관관계 관리")
	class 연관관계_관리 {

		@Test
		@DisplayName("이미 썸네일이 있으면 썸네일 이미지를 추가할 수 없다")
		void addImage_whenThumbnailAlreadyExists_throwsIllegalStateException() {
			// given
			Product product = createProduct();
			Image duplicateThumbnail = Image.create("https://example.com/thumb-duplicate.jpg", true);
            
            // when
            Throwable thrown = catchThrowable(() -> product.addImage(duplicateThumbnail));

            // then
            assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.THUMBNAIL_ONLY_ONE));
		}

		@Test
		@DisplayName("이미지 재등록 시 검증 후 기존 이미지를 교체한다")
		void registerImages_replacesImagesAfterValidation() {
			// given
			Product product = createProduct();

			// when
			List<Image> newImages = List.of(
				Image.create("https://example.com/main.jpg", true),
				Image.create("https://example.com/detail-1.jpg", false)
			);
			newImages.forEach(product::addImage);
			
			// then
			assertThat(product.getImages()).hasSize(2)
				.allSatisfy(image -> assertThat(image.getProduct()).isSameAs(product));
			long thumbnailCount = product.getImages().stream()
				.filter(image -> Boolean.TRUE.equals(image.getIsThumbnail()))
				.count();
			assertThat(thumbnailCount).isEqualTo(1);
		}

		@Test
		@DisplayName("썸네일 없이 이미지를 등록하면 예외가 발생한다")
		void registerImages_withoutThumbnail_throwsIllegalArgumentException() {
			// given
			Product product = createProduct();

			// when
            Throwable thrown = catchThrowable(() -> {
                List<Image> newImages = List.of(
                    Image.create("https://example.com/detail-1.jpg", false),
                    Image.create("https://example.com/detail-2.jpg", false)
                );
                newImages.forEach(product::addImage);
            });            // then
            assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.THUMBNAIL_ONLY_ONE));
        }


		@Test
		@DisplayName("서로 다른 옵션 조합은 정상적으로 추가된다")
		void addProductOption_withUniqueCombination_addsOption() {
			// given
			Product product = createProduct();
			ProductOption option = registerOption(product, 101L, 202L);

			// then
			assertThat(product.getProductOptions()).containsExactly(option);
			assertThat(option.getProduct()).isSameAs(product);
		}

		@Test
		@DisplayName("동일 옵션 조합을 추가하면 비즈니스 예외가 발생한다")
		void addProductOption_withDuplicateCombination_throwsBusinessException() {
			// given
			Product product = createProduct();
			registerOption(product, 11L, 22L);

			// when
			Throwable thrown = catchThrowable(() -> registerOption(product, 22L, 11L));

            // then
            assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_PRODUCT_OPTION));
			assertThat(product.getProductOptions()).hasSize(1);
		}
	}

	@Nested
	@DisplayName("정보 수정")
	class 정보_수정 {

		@Test
		@DisplayName("기본 정보를 변경하면 값이 갱신되고 true를 반환한다")
		void updateBasicInfo_whenValuesChanged_updatesAndReturnsTrue() {
			// given
			Product product = createProduct();

			// when
			boolean updated = product.updateBasicInfo("업데이트된 상품", "새로운 설명");

			// then
			assertThat(updated).isTrue();
			assertThat(product.getProductName()).isEqualTo("업데이트된 상품");
			assertThat(product.getProductInfo()).isEqualTo("새로운 설명");
			assertThat(product.getProductGenderType()).isEqualTo(ProductGenderType.WOMEN);
		}

		@Test
		@DisplayName("공백 이름으로 기본 정보를 변경하면 예외가 발생한다")
		void updateBasicInfo_withBlankName_throwsIllegalArgumentException() {
			// given
			Product product = createProduct();

			// when & then
			assertThatThrownBy(() -> product.updateBasicInfo(" ", null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.PRODUCT_INFO_REQUIRED));
		}

		@Test
		@DisplayName("판매 가능 여부를 변경하면 상태가 갱신된다")
		void changeAvailability_updatesFlag() {
			// given
			Product product = createProduct();

			// when
			product.changeAvailability(false);

			// then
			assertThat(product.getIsAvailable()).isFalse();

			// when
			product.changeAvailability(true);

			// then
			assertThat(product.getIsAvailable()).isTrue();
		}
	}

    // 상품 생성 헬퍼 메서드
	private Product createProduct() {
		Brand brand = Brand.create("신상", "SINSANG", BigDecimal.ONE);
		Image thumbnail = Image.create("https://example.com/thumbnail.jpg", true);

		return Product.builder()
			.brand(brand)
			.productName("테스트 상품")
			.productInfo("상세 설명")
			.productGenderType(ProductGenderType.MEN)
			.brandName("신상")
			.categoryPath("상의/티셔츠")
			.isAvailable(null)
			.images(List.of(thumbnail))
			.productOptions(null)
			.build();
	}

	// 상품 옵션 생성 헬퍼 메서드 - 상품이 직접 옵션을 생성하도록 위임한다.
	private ProductOption registerOption(Product product, long... optionValueIds) {
		Inventory inventory = Inventory.builder()
			.stockQuantity(new StockQuantity(10))
			.build();

		java.util.List<OptionValue> optionValues = new java.util.ArrayList<>();
		for (int i = 0; i < optionValueIds.length; i++) {
			OptionValue optionValue = OptionValue.builder()
				.optionName("옵션" + (i + 1))
				.optionValue("값" + optionValueIds[i])
				.build();
			ReflectionTestUtils.setField(optionValue, "optionValueId", optionValueIds[i]);
			optionValues.add(optionValue);
		}

		ProductOption productOption = ProductOption.create(product, new Money(BigDecimal.valueOf(19_900)), inventory);
		optionValues.forEach(optionValue -> {
			ProductOptionValue productOptionValue = ProductOptionValue.create(productOption, optionValue);
			productOption.addOptionValue(productOptionValue);
		});
		product.addProductOption(productOption);
		return productOption;
	}
}
