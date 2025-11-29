package com.mudosa.musinsa.product.infrastructure.cache;

import com.mudosa.musinsa.product.domain.model.OptionValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 옵션 값 ID에 대한 이름/값을 Redis에 캐싱한다.
 */
@Component
@RequiredArgsConstructor
public class OptionValueCache {

	private static final String KEY_PREFIX = "optionValue:";

	private final RedisTemplate<String, Object> redisTemplate;

	public void saveAll(Collection<OptionValue> optionValues) {
		if (optionValues == null || optionValues.isEmpty()) {
			return;
		}
		optionValues.forEach(this::save);
	}

	public void save(OptionValue optionValue) {
		if (optionValue == null || optionValue.getOptionValueId() == null) {
			return;
		}
		Value value = new Value(optionValue.getOptionName(), optionValue.getOptionValue());
		redisTemplate.opsForValue().set(buildKey(optionValue.getOptionValueId()), value);
	}

	public Value get(Long optionValueId) {
		if (optionValueId == null) {
			return null;
		}
		Object cached = redisTemplate.opsForValue().get(buildKey(optionValueId));
		return cached instanceof Value ? (Value) cached : null;
	}

    public Map<Long, Value> getAll(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return ids.stream()
            .map(id -> {
                Value value = get(id);
                return value != null ? Map.entry(id, value) : null;
            })
            .filter(entry -> entry != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

	private String buildKey(Long id) {
		return KEY_PREFIX + id;
	}

	/**
	 * 캐시에 저장할 값 객체.
	 */
	public record Value(String optionName, String optionValue) {}
}
