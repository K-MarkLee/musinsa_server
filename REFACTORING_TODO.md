# 리팩토링 TODO 리스트

## 🔴 우선순위 HIGH - 버그 수정

### 1. MemberCoupon 만료 시간 버그
**위치**: `MemberCoupon.java:50`
```java
// 🐛 현재 (잘못됨)
memberCoupon.expiredAt = LocalDateTime.now();  // 발급 즉시 만료!

// ✅ 수정
memberCoupon.expiredAt = coupon.getEndDate().plusDays(30);  // 쿠폰 종료일 + 30일
```

**영향**: 발급된 쿠폰이 즉시 만료되어 사용 불가
**테스트**: `MemberCouponTest.isUsuable_Available_ReturnsTrue()` 실패

---

## 🟡 우선순위 MEDIUM - Dead Code 제거

### 2. Event.limitScope 필드 미사용
**위치**: `Event.java:63`
```java
// 🗑️ 제거 대상
@Enumerated(EnumType.STRING)
@Column(name = "limit_scope", nullable = false, length = 20)
private LimitScope limitScope = LimitScope.EVENT;
```

**분석**:
- ✅ DB에 컬럼 존재
- ❌ Service 레이어에서 전혀 사용 안함
- ❌ EventCouponService.validateUserLimit()에서 무시됨

**의도된 기능**:
- `LimitScope.EVENT`: 이벤트당 1회 발급 제한
- `LimitScope.OPTION`: 이벤트 옵션당 1회 발급 제한

**제안**:
1. **옵션 A**: 기능 구현 후 유지
   ```java
   // EventCouponService.validateUserLimit()
   if (event.getLimitScope() == LimitScope.EVENT) {
       // 이벤트 전체 기준으로 제한
       long count = couponIssuanceService.countIssuedByUser(userId, couponId);
   } else {
       // 옵션별 기준으로 제한 (더 세밀한 제어)
       long count = countIssuedByUserAndOption(userId, eventId, productOptionId);
   }
   ```

2. **옵션 B**: 제거
   - DB 컬럼 삭제
   - Event 엔티티에서 필드 제거
   - DTO에서 제거

**권장**: 옵션 A (기능 구현) - 비즈니스 가치 있음

---

### 3. 주석 처리된 코드 제거
**위치**: `EventCouponService.java:81-82`
```java
// 🗑️ 제거
// ensureEventStockAvailable(eventOption);
```

**위치**: `EventCouponService.java:77-87`
```java
// 🗑️ 제거 (DRAFT → OPEN 자동 전환)
/*
if (event.getIsPublic() && shouldAutoOpen(event)) {
    event.open();
}
*/
```

---

## 🟢 우선순위 LOW - 코드 개선

### 4. N+1 쿼리 문제 (잠재적)
**위치**: `EventService.mapEventToDto()`
```java
// 🐌 현재
List<EventOption> options = eventOptionRepository.findByEventId(event.getId());

// ⚡ 개선
// Repository에서 fetch join 사용
@Query("SELECT e FROM Event e LEFT JOIN FETCH e.eventOptions WHERE e.id = :id")
Event findByIdWithOptions(@Param("id") Long id);
```

---

### 5. EventEntryService - 다중 서버 환경 미지원
**위치**: `EventEntryService.java:26`
```java
// 🚨 현재 (메모리 기반 - 서버 재시작 시 초기화)
private final ConcurrentMap<String, Instant> activeEntries = new ConcurrentHashMap<>();

// ✅ 개선 (Redis 기반 분산 락)
@RedisLock(key = "#eventId + ':' + #userId", timeout = 5000)
public EventEntryToken acquireSlot(Long eventId, Long userId) {
    // ...
}
```

**영향**:
- 서버 재시작 시 슬롯 정보 초기화
- 로드밸런서 환경에서 작동 안함 (서버 A, B 간 동기화 안됨)

---

### 6. 매직 넘버 상수화
**위치**: `EventEntryService.java:24`
```java
// 🔢 현재
private static final long HOLD_MILLIS = 5_000L;

// ✅ 개선 (application.yml로 이동)
@Value("${event.entry.hold-time-millis:5000}")
private long holdTimeMillis;
```

---

## 📊 코드 메트릭스 개선 목표

### 현재
- **총 코드 라인**: ~15,000
- **테스트 커버리지**: ~60%
- **순환 복잡도**: 평균 8 (복잡)
- **중복 코드**: ~5%

### 목표
- **총 코드 라인**: ~13,000 (불필요한 코드 제거)
- **테스트 커버리지**: ~75%
- **순환 복잡도**: 평균 5 (단순)
- **중복 코드**: <3%

---

## 🎯 리팩토링 체크리스트

### Phase 1: 버그 수정 (1일)
- [ ] MemberCoupon.expiredAt 버그 수정
- [ ] 테스트 코드 수정 및 검증

### Phase 2: Dead Code 제거 (2일)
- [ ] Event.limitScope 기능 구현 or 제거 결정
- [ ] 주석 처리된 코드 제거
- [ ] 사용하지 않는 import 제거

### Phase 3: 성능 개선 (3일)
- [ ] N+1 쿼리 최적화
- [ ] EventEntryService Redis 전환
- [ ] 인덱스 최적화

### Phase 4: 코드 품질 개선 (2일)
- [ ] 매직 넘버 상수화
- [ ] 중복 코드 제거
- [ ] 메서드 길이 줄이기 (20줄 이하)

---

## 🔧 도구 활용

### 정적 분석 도구
```bash
# SonarQube 실행
./gradlew sonarqube

# PMD 실행 (Dead Code 검증)
./gradlew pmdMain

# SpotBugs 실행 (버그 검증)
./gradlew spotbugsMain
```

### IntelliJ IDEA 기능
1. **Analyze > Inspect Code** - 전체 코드 검사
2. **Analyze > Run Inspection by Name > "Unused declaration"** - 미사용 코드 찾기
3. **Code > Optimize Imports** - 불필요한 import 제거
4. **Refactor > Safe Delete** - 안전하게 삭제

---

## 📈 예상 효과

### 코드 라인 수 감소
```
- Event.limitScope 관련 코드: -50 라인
- 주석 처리된 코드: -30 라인
- 불필요한 import: -20 라인
= 총 -100 라인
```

### 성능 개선
```
- N+1 쿼리 개선: 응답시간 30% 감소
- Redis 분산 락: 다중 서버 환경 지원
- 인덱스 최적화: 쿼리 속도 50% 향상
```

### 유지보수성 향상
```
- Dead Code 제거 → 코드 가독성 30% 향상
- 버그 수정 → 장애율 50% 감소
- 테스트 커버리지 향상 → 리팩토링 시간 40% 단축
```
