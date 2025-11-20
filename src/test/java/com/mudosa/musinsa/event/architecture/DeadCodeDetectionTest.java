package com.mudosa.musinsa.event.architecture;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Dead Code 검증 테스트
 * - 프로젝트 내 모든 엔티티의 사용되지 않는 필드 검증
 * - Service 레이어에서 실제로 호출되는지 확인
 */
@DisplayName("Dead Code 검증 테스트 - 전체 엔티티")
@SpringBootTest
@ActiveProfiles("test")
class DeadCodeDetectionTest {

    // Service 파일들이 있는 디렉토리 경로 (프로젝트에 맞게 수정)
    private static final String SERVICE_BASE_PATH = "src/main/java/com/mudosa/musinsa";

    // 제외할 필드명 (JPA 기본 필드, BaseEntity 필드 등)
    private static final Set<String> EXCLUDED_FIELDS = Set.of(
            "id", "createdAt", "updatedAt", "createdBy", "updatedBy",
            "serialVersionUID", "$jacocoData"
    );

    @Test
    @DisplayName("모든 엔티티의 사용되지 않는 필드 검출")
    void detectUnusedFieldsInAllEntities() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 Dead Code Detection - 모든 엔티티 스캔 시작");
        System.out.println("=".repeat(80) + "\n");

        // 1. 모든 엔티티 클래스 찾기
        Set<Class<?>> entityClasses = findAllEntityClasses();
        System.out.println("📦 발견된 엔티티: " + entityClasses.size() + "개\n");

        // 2. Service 레이어 소스 코드 읽기
        Map<String, String> serviceSourceCodes = loadAllServiceSourceCodes();
        System.out.println("📝 로드된 Service 파일: " + serviceSourceCodes.size() + "개\n");

        // 3. 각 엔티티의 필드 검사
        Map<String, List<String>> unusedFieldsByEntity = new LinkedHashMap<>();
        int totalFields = 0;
        int unusedFields = 0;

        for (Class<?> entityClass : entityClasses) {
            List<String> unused = checkEntityFields(entityClass, serviceSourceCodes);
            if (!unused.isEmpty()) {
                unusedFieldsByEntity.put(entityClass.getSimpleName(), unused);
                unusedFields += unused.size();
            }
            totalFields += getAllFields(entityClass).size();
        }

        // 4. 결과 출력
        printResults(unusedFieldsByEntity, totalFields, unusedFields);

        // 5. 선택적으로 assertion 실패 (경고만 할지, 실패할지 선택)
        // assertThat(unusedFieldsByEntity).isEmpty();
    }

    @Test
    @DisplayName("특정 엔티티의 필드 상세 분석")
    void analyzeSpecificEntity() {
        // 분석하고 싶은 엔티티 클래스명
        String targetEntityName = "Event";

        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔬 상세 분석: " + targetEntityName + " 엔티티");
        System.out.println("=".repeat(80) + "\n");

        Set<Class<?>> entityClasses = findAllEntityClasses();
        Class<?> targetEntity = entityClasses.stream()
                .filter(clazz -> clazz.getSimpleName().equals(targetEntityName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("엔티티를 찾을 수 없습니다: " + targetEntityName));

        Map<String, String> serviceSourceCodes = loadAllServiceSourceCodes();

        analyzeEntityInDetail(targetEntity, serviceSourceCodes);
    }

    /**
     * 프로젝트 내 모든 @Entity 클래스 찾기
     */
    private Set<Class<?>> findAllEntityClasses() {
        Reflections reflections = new Reflections("com.mudosa.musinsa");
        return reflections.getTypesAnnotatedWith(Entity.class);
    }

    /**
     * Service 레이어의 모든 소스 코드 로드
     */
    private Map<String, String> loadAllServiceSourceCodes() {
        Map<String, String> sourceCodes = new HashMap<>();

        try {
            Path servicePath = Paths.get(SERVICE_BASE_PATH);

            if (!Files.exists(servicePath)) {
                System.err.println("⚠️ Service 경로를 찾을 수 없습니다: " + servicePath);
                return sourceCodes;
            }

            try (Stream<Path> paths = Files.walk(servicePath)) {
                paths.filter(path -> path.toString().endsWith("Service.java"))
                        .forEach(path -> {
                            try {
                                String content = Files.readString(path);
                                sourceCodes.put(path.getFileName().toString(), content);
                            } catch (IOException e) {
                                System.err.println("파일 읽기 실패: " + path);
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("⚠️ Service 디렉토리 스캔 실패: " + e.getMessage());
        }

        return sourceCodes;
    }

    /**
     * 엔티티의 모든 필드 중 사용되지 않는 필드 찾기
     */
    private List<String> checkEntityFields(Class<?> entityClass, Map<String, String> serviceCodes) {
        List<String> unusedFields = new ArrayList<>();
        List<Field> fields = getAllFields(entityClass);

        for (Field field : fields) {
            String fieldName = field.getName();

            // 제외할 필드 스킵
            if (shouldSkipField(field)) {
                continue;
            }

            // Getter 메서드명 생성
            String getterName = generateGetterName(fieldName, field.getType());

            // Service에서 사용되는지 확인
            boolean isUsed = isFieldUsedInServices(fieldName, getterName, serviceCodes);

            if (!isUsed) {
                unusedFields.add(fieldName);
            }
        }

        return unusedFields;
    }

    /**
     * 엔티티의 모든 필드 가져오기 (상속된 필드 포함)
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * 필드를 스킵해야 하는지 확인
     */
    private boolean shouldSkipField(Field field) {
        String fieldName = field.getName();

        // 1. 제외 목록에 있는 필드
        if (EXCLUDED_FIELDS.contains(fieldName)) {
            return true;
        }

        // 2. static, transient 필드
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
            return true;
        }

        // 3. @Transient 어노테이션이 있는 필드
        if (field.isAnnotationPresent(Transient.class)) {
            return true;
        }

        // 4. 컬렉션 필드 (연관관계)
        if (Collection.class.isAssignableFrom(field.getType())) {
            return true;
        }

        // 5. $ 포함 (합성 필드)
        if (fieldName.contains("$")) {
            return true;
        }

        return false;
    }

    /**
     * Getter 메서드명 생성
     */
    private String generateGetterName(String fieldName, Class<?> fieldType) {
        String prefix = (fieldType == boolean.class || fieldType == Boolean.class) ? "is" : "get";
        return prefix + capitalize(fieldName);
    }

    /**
     * Service 코드에서 필드가 사용되는지 확인
     */
    private boolean isFieldUsedInServices(String fieldName, String getterName, Map<String, String> serviceCodes) {
        for (String sourceCode : serviceCodes.values()) {
            // Getter 메서드 호출 확인
            if (sourceCode.contains(getterName + "(")) {
                return true;
            }

            // 직접 필드명 사용 (리플렉션 등)
            if (sourceCode.contains("\"" + fieldName + "\"")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 상세 분석 (특정 엔티티)
     */
    private void analyzeEntityInDetail(Class<?> entityClass, Map<String, String> serviceCodes) {
        List<Field> fields = getAllFields(entityClass);

        System.out.println("📋 전체 필드: " + fields.size() + "개");
        System.out.println("\n필드별 사용 현황:\n");

        for (Field field : fields) {
            if (shouldSkipField(field)) {
                continue;
            }

            String fieldName = field.getName();
            String getterName = generateGetterName(fieldName, field.getType());
            boolean isUsed = isFieldUsedInServices(fieldName, getterName, serviceCodes);

            String status = isUsed ? "✅ 사용됨" : "❌ 미사용";
            System.out.printf("  %-30s %s%n", fieldName, status);

            // 사용되는 Service 파일 찾기
            if (isUsed) {
                List<String> usingServices = serviceCodes.entrySet().stream()
                        .filter(entry -> entry.getValue().contains(getterName + "("))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                if (!usingServices.isEmpty()) {
                    System.out.println("     └─ 사용 위치: " + String.join(", ", usingServices));
                }
            }
        }
    }

    /**
     * 결과 출력
     */
    private void printResults(Map<String, List<String>> unusedFieldsByEntity,
                              int totalFields, int unusedFields) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 분석 결과");
        System.out.println("=".repeat(80) + "\n");

        System.out.printf("전체 필드 수: %d개%n", totalFields);
        System.out.printf("사용되지 않는 필드: %d개 (%.1f%%)%n%n",
                unusedFields, (unusedFields * 100.0 / totalFields));

        if (unusedFieldsByEntity.isEmpty()) {
            System.out.println("✅ 모든 필드가 사용되고 있습니다!");
        } else {
            System.out.println("⚠️ 다음 필드들이 Service 레이어에서 사용되지 않습니다:\n");

            unusedFieldsByEntity.forEach((entityName, fields) -> {
                System.out.println("📦 " + entityName);
                fields.forEach(field -> System.out.println("   - " + field));
                System.out.println();
            });

            System.out.println("💡 리팩토링 제안:");
            System.out.println("   1. 실제로 사용할 비즈니스 로직을 구현");
            System.out.println("   2. 불필요하면 필드 제거 고려");
            System.out.println("   3. Repository에서만 사용되는 경우 추가 확인 필요");
        }

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}