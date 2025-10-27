package com.mudosa.musinsa.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mudosa.musinsa.exception.BusinessException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("결제 전략 팩토리 단위 테스트")
class PaymentStrategyFactoryTest {

	@Mock
	private TossPaymentStrategy tossStrategy;

	@Mock
	private KakaoFakePaymentStrategy kakaoStrategy;

	private PaymentStrategyFactory factory;

	@BeforeEach
	void setUp() {
		when(tossStrategy.getProviderName()).thenReturn("TOSS");
		when(kakaoStrategy.getProviderName()).thenReturn("KAKAO");

		List<PaymentStrategy> strategies = Arrays.asList(tossStrategy, kakaoStrategy);
		factory = new PaymentStrategyFactory(strategies);
	}

	@Test
	@DisplayName("전략 조회 성공 - TOSS")
	void getStrategy_Success_Toss() {
		// when
		PaymentStrategy strategy = factory.getStrategy("TOSS");

		// then
		assertThat(strategy).isNotNull();
		assertThat(strategy.getProviderName()).isEqualTo("TOSS");
		assertThat(strategy).isEqualTo(tossStrategy);
	}

	@Test
	@DisplayName("전략 조회 성공 - KAKAO")
	void getStrategy_Success_Kakao() {
		// when
		PaymentStrategy strategy = factory.getStrategy("KAKAO");

		// then
		assertThat(strategy).isNotNull();
		assertThat(strategy.getProviderName()).isEqualTo("KAKAO");
		assertThat(strategy).isEqualTo(kakaoStrategy);
	}

	@Test
	@DisplayName("전략 조회 실패 - 존재하지 않는 PG사")
	void getStrategy_Fail_NotFound() {
		// when & then
		assertThatThrownBy(() -> factory.getStrategy("NAVER"))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("PG");
	}

	@Test
	@DisplayName("전략 조회 실패 - null PG사")
	void getStrategy_Fail_NullProvider() {
		// when & then
		assertThatThrownBy(() -> factory.getStrategy(null))
				.isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("팩토리 생성 - 빈 전략 리스트")
	void factory_EmptyStrategies() {
		// given
		List<PaymentStrategy> emptyStrategies = List.of();

		// when
		PaymentStrategyFactory emptyFactory = new PaymentStrategyFactory(emptyStrategies);

		// then
		assertThatThrownBy(() -> emptyFactory.getStrategy("TOSS"))
				.isInstanceOf(BusinessException.class);
	}
}
