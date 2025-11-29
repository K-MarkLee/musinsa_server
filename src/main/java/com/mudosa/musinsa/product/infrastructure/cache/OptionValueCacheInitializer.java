package com.mudosa.musinsa.product.infrastructure.cache;

import com.mudosa.musinsa.product.domain.model.OptionValue;
import com.mudosa.musinsa.product.domain.repository.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 기동 시 옵션 값을 Redis에 적재한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OptionValueCacheInitializer {

	private final OptionValueRepository optionValueRepository;
	private final OptionValueCache optionValueCache;

	@EventListener(ApplicationReadyEvent.class)
	public void preloadOptionValues() {
		List<OptionValue> allOptionValues = optionValueRepository.findAll();
		optionValueCache.saveAll(allOptionValues);
		log.info("Preloaded option values into Redis cache. count={}", allOptionValues.size());
	}
}
