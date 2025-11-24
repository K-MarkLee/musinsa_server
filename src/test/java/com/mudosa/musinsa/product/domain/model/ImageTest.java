package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import com.mudosa.musinsa.exception.ErrorCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Image 도메인 모델의 테스트")
class ImageTest {

    @Test
    @DisplayName("이미지에 올바른 URL을 넣으면 이미지가 정상적으로 생성된다.")
    void createImage() {
        // given
        String imageUrl = "http://example.com/image.jpg";

        // when
        Image image = Image.create(null, imageUrl, false);

        // then
        assertThat(image.getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("이미지가 정상적으로 생성된다.")
    void createImageWithProduct() {
        // given
        Product product = Product.builder().build();
        String imageUrl = "http://example.com/image.jpg";

        // when
        Image image = Image.create(product, imageUrl, true);

        // then
        assertThat(image.getProduct()).isEqualTo(product);
        assertThat(image.getImageUrl()).isEqualTo(imageUrl);
        assertThat(image.getIsThumbnail()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    @DisplayName("이미지에 올바르지 못한(null, 빈값) URL 값을 넣으면 BusinessException이 발생한다.")
    void createImageWithInvalidUrl(String invalidUrl) {
        // when // then
        assertThatThrownBy(() -> Image.create(null, invalidUrl, false))
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.URL_REQUIRED);
    }

    @Test
	@DisplayName("이미지에 상품을 연결하면 연관관계가 설정된다.")
    void setProduct() {
        // given
        Product product = Product.builder().build();
        Image image = Image.builder()
                .imageUrl("http://example.com/image.jpg")
                .isThumbnail(false)
                .build();

        // when
        image.setProduct(product);

        //then
        assertThat(image.getProduct()).isEqualTo(product);
    }
}
