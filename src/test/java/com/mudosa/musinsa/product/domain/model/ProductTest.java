package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.brand.domain.model.Brand;
import com.mudosa.musinsa.common.vo.Money;
import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;
import com.mudosa.musinsa.product.domain.vo.StockQuantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product 애그리거트는 기본 정보 수정, 가용성 전환, 연관관계 설정을 지원해야 한다")
class ProductTest {

	@Nested
	@DisplayName("기본 정보 수정")
	class UpdateInfo {

		@Test
	@DisplayName("서로 다른 이름과 정보를 전달하면 updateBasicInfo 호출 시 값이 변경되고 true를 반환해야 한다")
		void givenDifferentValues_whenUpdateBasicInfo_thenChanged() {
			// given
			Brand brand = Brand.create("테스트", "TEST", BigDecimal.ZERO);
			Product product = Product.builder()
				.brand(brand)
				.productName("old")
				.productInfo("old info")
				.productGenderType(ProductGenderType.ALL)
				.brandName("테스트")
				.categoryPath("상의>티셔츠")
				.isAvailable(true)
				.build();

			// when
			boolean changed = product.updateBasicInfo("new", "new info");

			// then
			assertThat(changed).isTrue();
			assertThat(product.getProductName()).isEqualTo("new");
			assertThat(product.getProductInfo()).isEqualTo("new info");
		}

		@Test
	@DisplayName("공백 이름을 전달하면 updateBasicInfo 호출 시 BusinessException(ErrorCode.PRODUCT_INFO_REQUIRED)이 발생해야 한다")
		void givenBlankName_whenUpdateBasicInfo_thenThrows() {
			// given
			Product product = Product.builder()
				.brand(Brand.create("b", "b", BigDecimal.ZERO))
				.productName("name")
				.productInfo("info")
				.productGenderType(ProductGenderType.ALL)
				.brandName("b")
				.categoryPath("c")
				.isAvailable(true)
				.build();

			// when/then
			assertThatThrownBy(() -> product.updateBasicInfo(" ", "info"))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.PRODUCT_INFO_REQUIRED));
		}
	}

	@Nested
	@DisplayName("판매 가능 상태 변경")
	class Availability {

		@Test
	@DisplayName("isAvailable가 true일 때 changeAvailability(false) 호출 시 false로 변경되어야 하고, 다시 true로 변경되어야 한다")
		void givenInitialAvailable_whenChangeAvailability_thenToggles() {
			// given
			Product product = Product.builder()
				.brand(Brand.create("b", "b", BigDecimal.ZERO))
				.productName("n")
				.productInfo("i")
				.productGenderType(ProductGenderType.ALL)
				.brandName("b")
				.categoryPath("c")
				.isAvailable(true)
				.build();

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

	@Nested
	@DisplayName("연관관계 관리")
	class Associations {

		@Test
	@DisplayName("이미지를 생성하여 addImage 호출 시 Image의 product 참조가 설정되어야 한다")
		void givenImage_whenAddImage_thenProductReferenceSet() {
			// given
			Product product = Product.builder()
				.brand(Brand.create("b", "b", BigDecimal.ZERO))
				.productName("n")
				.productInfo("i")
				.productGenderType(ProductGenderType.ALL)
				.brandName("b")
				.categoryPath("c")
				.isAvailable(true)
				.build();

			// when
			Image img = Image.create("http://example.com/img.jpg", true);
			product.addImage(img);

			// then
			assertThat(product.getImages()).contains(img);
			assertThat(img.getProduct()).isEqualTo(product);
		}

		@Test
	@DisplayName("옵션과 재고 정보를 제공하면 addProductOption 호출 시 Product에 옵션이 추가되고 양방향 연관이 설정되어야 한다")
		void givenOption_whenAddProductOption_thenAssociated() {
			// given
			Product product = Product.builder()
				.brand(Brand.create("b", "b", BigDecimal.ZERO))
				.productName("n")
				.productInfo("i")
				.productGenderType(ProductGenderType.ALL)
				.brandName("b")
				.categoryPath("c")
				.isAvailable(true)
				.build();

			// when
			Inventory inventory = Inventory.builder().stockQuantity(new StockQuantity(5)).build();
			ProductOption option = ProductOption.create(product, new Money(1000L), inventory);
			product.addProductOption(option);

			// then
			assertThat(product.getProductOptions()).contains(option);
			assertThat(option.getProduct()).isEqualTo(product);
		}
	}
}
