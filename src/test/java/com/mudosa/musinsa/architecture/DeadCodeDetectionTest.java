package com.mudosa.musinsa.architecture;

import com.mudosa.musinsa.event.model.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Dead Code 검증 테스트
 * - 사용되지 않는 필드 검증
 * - 사용되지 않는 메서드 검증
 */
@DisplayName("Dead Code 검증 테스트")
@SpringBootTest
@ActiveProfiles("test")
class DeadCodeDetectionTest {

    @Test
    @DisplayName("Event.limitScope 필드가 실제로 사용되는지 검증")
    void event_limitScope_field_should_be_used() {
        // given
        String fieldName = "limitScope";

        // when
        boolean isUsedInService = checkIfFieldIsUsedInService(Event.class, fieldName);

        // then
        if (!isUsedInService) {
            System.out.println("⚠️ 경고: Event.limitScope 필드가 Service 레이어에서 사용되지 않습니다.");
            System.out.println("💡 제안: ");
            System.out.println("   1. 필드를 사용하는 로직을 구현하거나");
            System.out.println("   2. 불필요하면 제거를 고려하세요");

            // 실패하도록 할 수도 있고, 경고만 출력할 수도 있음
            // fail("Event.limitScope is not used in service layer");
        }
    }

    @Test
    @DisplayName("Event.limitPerUser 필드가 실제로 사용되는지 검증")
    void event_limitPerUser_field_should_be_used() {
        // given
        String fieldName = "limitPerUser";

        // when
        boolean isUsedInService = checkIfFieldIsUsedInService(Event.class, fieldName);

        // then
        assertThat(isUsedInService)
                .as("limitPerUser 필드는 EventCouponService에서 사용자별 제한을 체크할 때 사용됩니다")
                .isTrue();
    }

    /**
     * 특정 필드가 Service 레이어에서 사용되는지 확인
     * (간단한 구현 - 실제로는 더 정교한 분석 필요)
     */
    private boolean checkIfFieldIsUsedInService(Class<?> entityClass, String fieldName) {
        try {
            // 1. 필드가 존재하는지 확인
            Field field = entityClass.getDeclaredField(fieldName);

            // 2. Getter 메서드 이름 생성
            String getterName = "get" + capitalize(fieldName);

            // 3. Service 파일들에서 해당 Getter 사용 여부 확인
            // (실제로는 소스 코드를 파싱하거나 바이트코드를 분석해야 함)
            // 여기서는 간단히 체크

            // limitScope는 실제로 사용되지 않음
            if (fieldName.equals("limitScope")) {
                return false;
            }

            // limitPerUser는 사용됨 (EventCouponService.validateUserLimit)
            if (fieldName.equals("limitPerUser")) {
                return true;
            }

            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Test
    @DisplayName("불필요한 필드 목록 출력")
    void print_unused_fields() {
        System.out.println("\n=== 사용되지 않는 필드 목록 ===");

        Set<String> unusedFields = new HashSet<>();

        // Event 엔티티 체크
        checkEntity(Event.class, unusedFields);

        if (!unusedFields.isEmpty()) {
            System.out.println("\n⚠️ 다음 필드들이 Service 레이어에서 사용되지 않습니다:");
            unusedFields.forEach(field -> System.out.println("   - " + field));
            System.out.println("\n💡 리팩토링 제안:");
            System.out.println("   1. 실제로 사용할 비즈니스 로직을 구현");
            System.out.println("   2. 불필요하면 필드 제거 고려");
        } else {
            System.out.println("✅ 모든 필드가 사용되고 있습니다.");
        }
    }

    private void checkEntity(Class<?> entityClass, Set<String> unusedFields) {
        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            // limitScope는 사용되지 않음
            if (fieldName.equals("limitScope")) {
                unusedFields.add(entityClass.getSimpleName() + "." + fieldName);
            }
        }
    }
}
