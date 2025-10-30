# 무신사 클론 프로젝트 - 서버 💻

> 무신사 쇼핑몰 클론 코딩 프로젝트의 백엔드 서버입니다.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

---

## 🚀 빠른 시작

### 1. 필수 요구사항

- **Java 21** 이상
- **MySQL 8.0** 이상
- **Gradle 8.x** (Wrapper 포함)
- **IntelliJ IDEA** (권장)

### 2. 환경 설정 (3분 완성)

```bash
# 1. MySQL 데이터베이스 생성
mysql -u root -p
> CREATE DATABASE musinsa_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> CREATE USER 'musinsa_user'@'localhost' IDENTIFIED BY 'your_password';
> GRANT ALL PRIVILEGES ON musinsa_dev.* TO 'musinsa_user'@'localhost';
> FLUSH PRIVILEGES;
> exit;

# 2. 환경 변수 파일 생성
cp .env.dev.example .env.dev
# .env.dev 파일을 열어서 DB 비밀번호 수정

# 3. 의존성 다운로드 및 빌드
./gradlew clean build -x test

# 4. 스키마 적용 (IntelliJ DB Tool에서)
# db/schema/ 폴더의 SQL 파일들을 순서대로 실행
```

### 3. 애플리케이션 실행

```bash
# 방법 1: Gradle
./gradlew bootRun --args='--spring.profiles.active=dev'

# 방법 2: IntelliJ
# Run 'ServerApplication' (Shift + F10)
```

### 4. 확인

```bash
# Health Check
curl http://localhost:8080/actuator/health
# 응답: {"status":"UP"}

# Swagger UI 접속
http://localhost:8080/swagger-ui.html
```

**🎉 완료! 개발을 시작하세요!**

---

## 📁 프로젝트 구조

```
server/
├── src/
│   ├── main/
│   │   ├── java/com/mudosa/musinsa/
│   │   │   ├── domain/              # 도메인 모델
│   │   │   │   ├── orders/           # 주문 도메인
│   │   │   │   ├── payment/         # 결제 도메인
│   │   │   │   ├── user/            # 회원 도메인
│   │   │   │   ├── product/         # 상품 도메인
│   │   │   │   ├── brand/           # 브랜드 도메인
│   │   │   │   ├── coupon/          # 쿠폰 도메인
│   │   │   │   └── event/           # 이벤트 도메인
│   │   │   ├── global/              # 공통 설정
│   │   │   │   ├── config/          # Spring 설정
│   │   │   │   ├── security/        # Security & JWT
│   │   │   │   ├── exception/       # 예외 처리
│   │   │   │   └── util/            # 유틸리티
│   │   │   └── ServerApplication.java
│   │   └── resources/
│   │       ├── application.yml       # 공통 설정
│   │       ├── application-dev.yml   # 개발 환경
│   │       ├── application-test.yml  # 테스트 환경
│   │       ├── application-prod.yml  # 프로덕션 환경
│   │       └── mappers/              # MyBatis XML
│   └── test/
├── db/
│   └── schema/                       # 데이터베이스 스키마
├── docs/                             # 프로젝트 문서
│   ├── SETUP.md                      # 📘 상세 환경 설정 가이드
│   ├── MYBATIS_GUIDE.md              # 📗 MyBatis 사용 가이드
│   ├── PROFILE_COMPARISON.md         # 📊 프로필별 설정 비교
│   └── CONFIG_SUMMARY.md             # 📄 설정 빠른 참조
├── .env.dev                          # 개발 환경 변수 (Git 제외)
├── .env.test                         # 테스트 환경 변수 (Git 제외)
├── .env.prod.example                 # 프로덕션 환경 변수 예시
├── build.gradle                      # 빌드 설정
└── README.md                         # 이 파일
```

---

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.5.6** - 최신 Spring Framework
- **Java 21** - LTS 버전
- **Gradle 8.x** - 빌드 도구

### Database & ORM
- **MySQL 8.0** - 관계형 데이터베이스
- **Spring Data JPA** - ORM (단순 CRUD)
- **MyBatis 3.0.5** - SQL Mapper (복잡한 쿼리)
- **HikariCP** - 고성능 커넥션 풀

### Security & Authentication
- **Spring Security** - 인증/인가
- **JWT (jjwt 0.12.6)** - 토큰 기반 인증

### API Documentation
- **SpringDoc OpenAPI 2.3.0** - Swagger UI

### Utilities
- **Lombok** - 보일러플레이트 코드 제거
- **Validation** - Bean Validation

---

## 📚 문서

### 필수 문서
- **[환경 설정 가이드](docs/SETUP.md)** - 상세한 로컬 환경 구축 방법 📘
- **[MyBatis 가이드](docs/MYBATIS_GUIDE.md)** - MyBatis 작성 규칙 및 Best Practice 📗
- **[프로필 비교표](docs/PROFILE_COMPARISON.md)** - Dev/Test/Prod 설정 비교 📊
- **[설정 빠른 참조](docs/CONFIG_SUMMARY.md)** - 핵심 설정 요약 📄

### 데이터베이스
- **[스키마 정의](db/schema/)** - 테이블 생성 스크립트

---

## 🎯 도메인별 기능

### 주문/결제 (Order & Payment)
- 주문 생성 및 조회
- 결제 처리
- 결제 로그 관리

### 회원 (User)
- 회원 가입 및 로그인
- JWT 기반 인증
- 회원 정보 관리

### 상품 (Product)
- 상품 조회
- 상품 옵션 관리
- 재고 관리

### 브랜드 (Brand)
- 브랜드 정보 관리
- 브랜드별 상품 조회

### 쿠폰/이벤트 (Coupon & Event)
- 쿠폰 발급 및 사용
- 이벤트 관리

---

## 👥 팀원 및 역할

| 역할 | 담당 기능 | 상태 |
|-----|----------|------|
| 주문/결제 | Order, Payment, PaymentLog | 🚧 개발 중 |
| 상품 | Product, ProductOption | 🚧 개발 중 |
| 회원 | User, Auth | 🚧 개발 중 |
| 브랜드 | Brand | 🚧 개발 중 |
| 쿠폰/이벤트 | Coupon, Event | 🚧 개발 중 |

---

## 🔄 개발 워크플로우

### 브랜치 전략 (Git Flow)

```
main                    # 프로덕션 배포 브랜치
  └── develop           # 개발 통합 브랜치
        ├── feature/*   # 기능 개발 브랜치
        └── hotfix/*    # 긴급 수정 브랜치
```

### 스키마 변경 프로세스

1. `db/schema/Vx__description.sql` 파일 생성
2. 로컬에서 테스트
3. Git 커밋 & PR 생성
4. 리뷰 후 머지
5. 팀원들이 Pull 받아서 수동으로 SQL 실행

### 커밋 컨벤션

```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드, 패키지 설정
```

---

## 🧪 테스트

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests OrderServiceTest

# 테스트 커버리지
./gradlew test jacocoTestReport
```

### 테스트 환경

```bash
# 테스트 프로필 자동 적용
@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceTest {
    // ...
}
```

---

## 🔧 환경별 설정

### Dev (로컬 개발)

```yaml
프로필: dev
목적: 로컬에서 디버깅
특징:
  - ✅ 모든 SQL 로그 출력
  - ✅ Swagger UI 활성화
  - ✅ 상세한 에러 메시지
  - 🔍 디버깅 최적화
```

### Test (테스트)

```yaml
프로필: test
목적: 자동화 테스트
특징:
  - ⚡ 빠른 실행
  - 🗄️ DB 자동 생성/삭제
  - 📝 최소 로깅
  - 🔒 격리된 환경
```

### Prod (프로덕션)

```yaml
프로필: prod
목적: 실제 서비스
특징:
  - 🚀 최고 성능
  - 🔒 최대 보안
  - 📝 최소 로깅
  - ⚡ 캐시 최대 활용
```

**[📊 상세 비교표 보기](docs/PROFILE_COMPARISON.md)**

---

## 🔐 보안 체크리스트

### 개발 환경

- [x] `.env` 파일이 `.gitignore`에 포함됨
- [x] 하드코딩된 비밀번호 없음
- [x] JWT Secret은 환경 변수로 관리

### 프로덕션 환경

- [ ] JWT Secret이 256bit 이상
- [ ] SSL/TLS 활성화
- [ ] Swagger UI 비활성화
- [ ] 에러 상세 정보 노출 방지
- [ ] DB 접근 권한 최소화

---

## 🚨 트러블슈팅

### 자주 발생하는 문제

| 문제 | 해결 방법 |
|------|----------|
| **DB 연결 실패** | MySQL 실행 확인, `.env.dev` 설정 확인 |
| **JWT 길이 오류** | Secret 256bit 이상으로 변경 |
| **스키마 검증 실패** | Entity와 DB 스키마 일치 확인 |
| **Mapper 없음** | `mapper-locations` 경로 확인 |
| **커넥션 풀 고갈** | `maximum-pool-size` 증가 |

**[🔍 상세 트러블슈팅 가이드](docs/SETUP.md#문제-해결)**

---

## 📊 JPA vs MyBatis 사용 기준

### JPA 사용 (권장)

- ✅ 단순 CRUD
- ✅ Entity 간 연관관계
- ✅ 트랜잭션 내 여러 작업

```java
// JPA 예시
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Integer userId);
}
```

### MyBatis 사용 (권장)

- ✅ 복잡한 조인 (3개 이상 테이블)
- ✅ 집계 쿼리 (GROUP BY, HAVING)
- ✅ 대량 데이터 조회
- ✅ 동적 쿼리

```xml
<!-- MyBatis 예시 -->
<select id="getOrderStatistics" resultType="map">
    SELECT DATE(o.created_at) AS date,
           COUNT(*) AS count,
           SUM(o.final_payment_amount) AS revenue
    FROM `Order` o
    WHERE o.created_at BETWEEN #{startDate} AND #{endDate}
    GROUP BY DATE(o.created_at)
</select>
```

**[📗 MyBatis 상세 가이드](docs/MYBATIS_GUIDE.md)**

---

## 📦 빌드 & 배포

### 로컬 빌드

```bash
# JAR 파일 생성
./gradlew build

# 빌드 결과물
ls -lh build/libs/
# server-0.0.1-SNAPSHOT.jar
```

### 프로덕션 실행

```bash
# 환경 변수 파일과 함께 실행
java -jar \
  -Dspring.profiles.active=prod \
  build/libs/server-0.0.1-SNAPSHOT.jar
```

### Docker (추후 추가 예정)

```dockerfile
# Dockerfile
FROM openjdk:21-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 📈 성능 최적화

### HikariCP 설정

```yaml
# 프로덕션 최적화
hikari:
  maximum-pool-size: 20
  data-source-properties:
    cachePrepStmts: true
    prepStmtCacheSize: 250
    useServerPrepStmts: true
```

### JPA 배치 처리

```yaml
jpa:
  properties:
    hibernate:
      jdbc.batch_size: 100
      order_inserts: true
      order_updates: true
```

### MyBatis 캐싱

```yaml
mybatis:
  configuration:
    cache-enabled: true
    local-cache-scope: SESSION
```

---

## 📝 API 문서

### Swagger UI

**개발 환경에서만 접근 가능**

```
http://localhost:8080/swagger-ui.html
```

### API Docs (JSON)

```
http://localhost:8080/api-docs
```

---

## 🔗 유용한 링크

### 공식 문서
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [MyBatis Documentation](https://mybatis.org/mybatis-3/)
- [HikariCP GitHub](https://github.com/brettwooldridge/HikariCP)

### 팀 리소스
- Slack: `#musinsa-backend`
- Jira: `MUSINSA` 프로젝트
- Confluence: 기술 문서

---

## 🤝 기여 가이드

### 코드 리뷰 기준

- [ ] 코딩 컨벤션 준수
- [ ] 테스트 코드 작성
- [ ] 문서 업데이트
- [ ] 불필요한 로그 제거

### Pull Request

1. `develop` 브랜치에서 `feature/기능명` 브랜치 생성
2. 기능 개발 및 테스트
3. PR 생성 (템플릿 작성)
4. 코드 리뷰 및 승인 (최소 1명)
5. `develop`에 머지

---

## 📄 라이선스

MIT License (또는 프로젝트에 맞는 라이선스)

---

## 💬 문의

- **기술 문의**: Slack `#musinsa-backend`
- **버그 리포트**: GitHub Issues
- **문서 개선**: Pull Request 환영!

---

<div align="center">

**무신사 클론 프로젝트 - Backend Team**

Made with ❤️ by Backend Developers

</div>
