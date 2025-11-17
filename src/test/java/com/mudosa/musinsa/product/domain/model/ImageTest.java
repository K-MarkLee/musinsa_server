package com.mudosa.musinsa.product.domain.model;

import com.mudosa.musinsa.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Image 엔티티는 URL 필수 검증과 썸네일 속성 설정을 보장해야 한다")
class ImageTest {

    @Test
    @DisplayName("유효한 URL과 isThumbnail 값을 전달하면 Image가 생성되어 해당 값들이 설정되어야 한다")
    void create_happyPath() {
        // given
        String url = "http://example.com/x.jpg";

        // when
        Image img = Image.create(url, true);

        // then
        assertThat(img.getImageUrl()).isEqualTo(url);
        assertThat(img.getIsThumbnail()).isTrue();
    }

    @Test
    @DisplayName("null 또는 공백 URL을 전달하면 Image.create 호출 시 BusinessException이 발생해야 한다")
    void create_invalidUrl_throws() {
        // when / then
        assertThatThrownBy(() -> Image.create(null, false)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> Image.create(" ", false)).isInstanceOf(BusinessException.class);
    }
}
