# ✅ 프로젝트 설정 완료 체크리스트

## 📋 목차
1. [초기 설정](#초기-설정)
2. [개발 환경 확인](#개발-환경-확인)
3. [Git 커밋 전](#git-커밋-전)
4. [프로덕션 배포 전](#프로덕션-배포-전)

---

## 🚀 초기 설정

### 1. 필수 소프트웨어 설치

- [ ] Java 21 설치 확인
  ```bash
  java -version
  # 출력: openjdk version "21.x.x"
  ```

- [ ] MySQL 8.0 설치 및 실행 확인
  ```bash
  mysql --version
  # 출력: mysql  Ver 8.0.x
  
  # MySQL 실행 확인
  brew services list  # Mac
  systemctl status mysql  # Linux
  ```

- [ ] Gradle 확인 (Wrapper 사용)
  ```bash
  ./gradlew --version
  ```

### 2. 데이터베이스 생성

- [ ] 개발용 DB 생성
  ```sql
  CREATE DATABASE musinsa_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

- [ ] 테스트용 DB 생성
  ```sql
  CREATE DATABASE musinsa_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```

- [ ] DB 사용자 생성 및 권한 부여
  ```sql
  CREATE USER 'musinsa_user'@'localhost' IDENTIFIED BY 'your_password';
  GRANT ALL PRIVILEGES ON musinsa_dev.* TO 'musinsa_user'@'localhost';
  GRANT ALL PRIVILEGES ON musinsa_test.* TO 'musinsa_user'@'localhost';
  FLUSH PRIVILEGES;
  ```

### 3. 환경 변수 파일 생성

- [ ] `.env.dev` 파일 생성 및 수정
  ```bash
  # 파일 존재 확인
  ls -la .env.dev
  
  # 내용 확인
  cat .env.dev
  ```

- [ ] 필수 환경 변수 설정 확인
  - [ ] `DB_HOST`
  - [ ] `DB_PORT`
  - [ ] `DB_NAME`
  - [ ] `DB_USERNAME`
  - [ ] `DB_PASSWORD`
  - [ ] `JWT_SECRET` (256bit 이상)

### 4. 스키마 적용

- [ ] IntelliJ DB Tool 연결
  ```
  View → Tool Windows → Database
  + → Data Source → MySQL
  ```

- [ ] `db/schema/` 파일 순서대로 실행
  - [ ] `V1__init_schema.sql`
  - [ ] `V2__create_payment_tables.sql`
  - [ ] (기타 스키마 파일...)

### 5. 빌드 및 실행

- [ ] 의존성 다운로드 및 빌드
  ```bash
  ./gradlew clean build -x test
  ```

- [ ] 애플리케이션 실행
  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=dev'
  ```

- [ ] 헬스체크 확인
  ```bash
  curl http://localhost:8080/actuator/health
  # 예상 응답: {"status":"UP"}
  ```

- [ ] Swagger UI 접속 확인
  ```
  http://localhost:8080/swagger-ui.html
  ```

---

## 🔍 개발 환경 확인

### 설정 파일 검증

- [ ] `application.yml` 공통 설정 확인
  - [ ] JPA 기본 설정
  - [ ] MyBatis mapper-locations
  - [ ] Jackson 날짜 형식
  - [ ] Swagger 설정

- [ ] `application-dev.yml` 개발 설정 확인
  - [ ] `ddl-auto: validate`
  - [ ] `show-sql: true`
  - [ ] 로깅 레벨: debug/trace
  - [ ] Swagger enabled: true

- [ ] `application-test.yml` 테스트 설정 확인
  - [ ] `ddl-auto: create-drop`
  - [ ] 로깅 최소화
  - [ ] Swagger enabled: false

- [ ] `application-prod.yml` 프로덕션 설정 확인
  - [ ] `ddl-auto: none`
  - [ ] `show-sql: false`
  - [ ] Swagger enabled: false
  - [ ] 성능 최적화 설정

### 의존성 확인

- [ ] `build.gradle` 의존성 확인
  - [ ] Spring Boot Starter Web
  - [ ] Spring Boot Starter Security
  - [ ] Spring Boot Starter Data JPA
  - [ ] MyBatis Spring Boot Starter
  - [ ] MySQL Connector
  - [ ] JWT (jjwt)
  - [ ] SpringDoc OpenAPI
  - [ ] Lombok

### 로깅 확인

- [ ] 애플리케이션 실행 시 로그 출력 확인
  ```
  # JPA SQL 로그
  Hibernate: SELECT * FROM ...
  
  # MyBatis SQL 로그
  ==>  Preparing: SELECT * FROM ...
  ==> Parameters: ...
  ```

- [ ] HikariCP 커넥션 풀 로그 확인
  ```
  HikariPool-Dev - Starting...
  HikariPool-Dev - Start completed.
  ```

### 기능 테스트

- [ ] 테스트 실행 확인
  ```bash
  ./gradlew test
  # 모든 테스트 통과 확인
  ```

- [ ] 간단한 API 테스트
  ```bash
  # Health Check
  curl http://localhost:8080/actuator/health
  
  # Metrics
  curl http://localhost:8080/actuator/metrics
  ```

---

## 📝 Git 커밋 전

### 보안 체크

- [ ] `.env.dev` 파일이 `.gitignore`에 포함되어 있는가?
  ```bash
  cat .gitignore | grep .env
  ```

- [ ] 하드코딩된 비밀번호가 없는가?
  ```bash
  # application*.yml 파일 검토
  grep -r "password" src/main/resources/
  ```

- [ ] JWT Secret이 환경 변수로 관리되는가?
  ```bash
  grep "JWT_SECRET" src/main/resources/application*.yml
  ```

### 코드 품질

- [ ] 불필요한 주석 제거
- [ ] `System.out.println()` 대신 Logger 사용
- [ ] 미사용 import 제거
- [ ] 코딩 컨벤션 준수

### 테스트

- [ ] 모든 테스트 통과
  ```bash
  ./gradlew test
  ```

- [ ] 새로운 기능에 대한 테스트 작성
- [ ] 빌드 성공 확인
  ```bash
  ./gradlew clean build
  ```

### 문서

- [ ] README.md 업데이트 (필요 시)
- [ ] CHANGELOG.md 업데이트 (필요 시)
- [ ] API 문서 확인 (Swagger)

### Git

- [ ] `.gitignore` 확인
- [ ] 커밋 메시지 작성 (컨벤션 준수)
  ```
  feat: 주문 생성 API 구현
  fix: 결제 금액 계산 버그 수정
  docs: README 환경 설정 가이드 추가
  ```

- [ ] 브랜치 확인
  ```bash
  git branch
  # * feature/orders-api
  ```

---

## 🚀 프로덕션 배포 전

### 환경 설정

- [ ] 프로덕션 환경 변수 파일 준비 (`.env.prod`)
  - [ ] DB_HOST (프로덕션 DB 주소)
  - [ ] DB_PASSWORD (강력한 비밀번호)
  - [ ] JWT_SECRET (256bit 이상, `openssl rand -base64 64`)
  - [ ] JWT_ACCESS_EXPIRATION (30분 권장)

- [ ] `application-prod.yml` 설정 확인
  - [ ] `spring.profiles.active=prod`
  - [ ] `ddl-auto: none` ⚠️
  - [ ] `show-sql: false`
  - [ ] SSL 설정

### 보안

- [ ] JWT Secret이 안전하게 관리되는가?
- [ ] DB 비밀번호가 강력한가?
- [ ] Swagger가 비활성화되었는가?
- [ ] 에러 상세 정보가 노출되지 않는가?
  - [ ] `include-stacktrace: never`
  - [ ] `include-exception: false`

### 성능

- [ ] HikariCP 설정 최적화
  - [ ] `maximum-pool-size: 20`
  - [ ] PreparedStatement 캐싱 활성화

- [ ] JPA 성능 설정
  - [ ] `generate_statistics: false`
  - [ ] 배치 처리 활성화

- [ ] MyBatis 캐싱 설정
  - [ ] `cache-enabled: true`
  - [ ] `local-cache-scope: SESSION`

### 모니터링

- [ ] 로깅 파일 경로 설정
  ```yaml
  logging:
    file:
      name: /var/log/musinsa/application.log
  ```

- [ ] Actuator 엔드포인트 설정
  - [ ] `health`
  - [ ] `metrics`
  - [ ] `prometheus` (필요 시)

### 테스트

- [ ] 프로덕션 환경과 유사한 환경에서 테스트
- [ ] 성능 테스트 (부하 테스트)
- [ ] 장애 복구 시나리오 테스트

### 백업

- [ ] DB 백업 계획 수립
- [ ] 롤백 계획 수립
- [ ] 장애 대응 매뉴얼 작성

### 배포

- [ ] 빌드 파일 생성
  ```bash
  ./gradlew clean build -x test
  ```

- [ ] JAR 파일 확인
  ```bash
  ls -lh build/libs/
  ```

- [ ] 배포 스크립트 준비
- [ ] 무중단 배포 설정 (필요 시)

### 배포 후 확인

- [ ] 애플리케이션 정상 기동 확인
  ```bash
  curl https://your-domain.com/actuator/health
  ```

- [ ] 로그 모니터링
  ```bash
  tail -f /var/log/musinsa/application.log
  ```

- [ ] 주요 API 동작 확인
- [ ] 에러 로그 확인

---

## 📊 프로필별 체크리스트

### Dev Profile

- [x] `ddl-auto: validate`
- [x] `show-sql: true`
- [x] Logging: DEBUG/TRACE
- [x] Swagger: Enabled
- [x] Connection Pool: 10

### Test Profile

- [x] `ddl-auto: create-drop`
- [x] Logging: Minimal
- [x] Swagger: Disabled
- [x] Connection Pool: 5

### Prod Profile

- [x] `ddl-auto: none`
- [x] `show-sql: false`
- [x] Logging: WARN/INFO
- [x] Swagger: Disabled
- [x] Connection Pool: 20
- [x] SSL: Required
- [x] Performance Optimization

---

## 🔧 트러블슈팅 체크리스트

### DB 연결 실패 시

- [ ] MySQL 서비스 실행 중인가?
- [ ] `.env.dev` 파일의 DB 정보가 정확한가?
- [ ] 방화벽 설정 확인
- [ ] DB 사용자 권한 확인

### JWT 오류 시

- [ ] JWT Secret 길이가 256bit 이상인가?
- [ ] 환경 변수가 올바르게 설정되었는가?

### 스키마 검증 실패 시

- [ ] Entity와 DB 스키마가 일치하는가?
- [ ] 모든 SQL 파일이 실행되었는가?

### 커넥션 풀 고갈 시

- [ ] `maximum-pool-size` 증가
- [ ] 커넥션 누수 확인 (`leak-detection-threshold`)
- [ ] 트랜잭션이 제대로 종료되는가?

---

## 📚 참고 문서

- [환경 설정 가이드](docs/SETUP.md)
- [프로필 비교표](docs/PROFILE_COMPARISON.md)
- [MyBatis 가이드](docs/MYBATIS_GUIDE.md)
- [설정 빠른 참조](docs/CONFIG_SUMMARY.md)

---

## ✨ 최종 점검

### 초급 개발자 체크리스트

- [ ] 로컬 환경 구축 완료
- [ ] 애플리케이션 실행 성공
- [ ] 간단한 API 테스트 성공
- [ ] Git 커밋 및 푸시 완료

### 중급 개발자 체크리스트

- [ ] 프로필별 설정 이해
- [ ] JPA vs MyBatis 사용 기준 이해
- [ ] 테스트 코드 작성
- [ ] 성능 최적화 적용

### 고급 개발자 체크리스트

- [ ] 프로덕션 배포 계획 수립
- [ ] 모니터링 및 알람 설정
- [ ] 장애 대응 시나리오 작성
- [ ] 성능 튜닝 및 프로파일링

---

**체크리스트 완료 후 개발을 시작하세요! 🚀**
