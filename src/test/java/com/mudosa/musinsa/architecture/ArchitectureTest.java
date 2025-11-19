package com.mudosa.musinsa.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

/**
 * 아키텍처 규칙 검증 테스트
 * - 불필요한 필드 검증
 * - Dead Code 검증
 * - 레이어 간 의존성 검증
 */
@DisplayName("아키텍처 규칙 테스트")
class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.mudosa.musinsa";
    private final JavaClasses classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    @DisplayName("Service 레이어는 Presentation 레이어를 참조하면 안된다")
    void service_should_not_depend_on_presentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat()
                .resideInAPackage("..presentation..");

        rule.check(classes);
    }

    @Test
    @DisplayName("Repository는 Service를 참조하면 안된다")
    void repository_should_not_depend_on_service() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat()
                .resideInAPackage("..service..");

        rule.check(classes);
    }

    @Test
    @DisplayName("필드 주입(@Autowired)을 사용하면 안된다")
    void no_field_injection() {
        NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(classes);
    }

    @Test
    @DisplayName("Entity는 Setter를 가지면 안된다 (불변성)")
    void entity_should_not_have_setters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..model..")
                .should().haveMethodsThat()
                .haveName("set.*");

        rule.check(classes);
    }
}
