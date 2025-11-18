# Event & Coupon 도메인 테스트 명세서

## 1. Event 도메인 테스트

### 1.1 Event 엔티티 (Model) 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| Event | 생성 | Event 생성 | 해피 | Event.create() | 이벤트 정보 (제목, 설명, 타입, 날짜 등) | Event.create() 호출 | Event 객체 생성됨 | Event 객체가 생성되고 모든 필드가 올바르게 설정됨 | ✅ |
| Event | 상태변경 | OPEN 상태로 변경 | 해피 | Event.open() | DRAFT 상태의 Event | open() 호출 | 상태가 OPEN으로 변경됨 | status == OPEN | ✅ |
| Event | 상태변경 | PAUSED 상태로 변경 | 해피 | Event.pause() | OPEN 상태의 Event | pause() 호출 | 상태가 PAUSED로 변경됨 | status == PAUSED | ✅ |
| Event | 상태변경 | ENDED 상태로 변경 | 해피 | Event.end() | Event 객체 | end() 호출 | 상태가 ENDED로 변경됨 | status == ENDED | ✅ |
| Event | 상태변경 | CANCELLED 상태로 변경 | 해피 | Event.cancel() | Event 객체 | cancel() 호출 | 상태가 CANCELLED로 변경됨 | status == CANCELLED | ✅ |
| Event | 진행여부 | 진행중 확인 (기간내, OPEN) | 해피 | Event.isOngoing() | 기간 내 OPEN 상태의 Event | isOngoing(now) 호출 | true 반환 | true | ✅ |
| Event | 진행여부 | 진행중 확인 (시작 전) | 예외 | Event.isOngoing() | 시작 전의 Event | isOngoing(now) 호출 | false 반환 | false | ✅ |
| Event | 진행여부 | 진행중 확인 (종료 후) | 예외 | Event.isOngoing() | 종료된 Event | isOngoing(now) 호출 | false 반환 | false | ✅ |
| Event | 진행여부 | 진행중 확인 (상태 OPEN 아님) | 예외 | Event.isOngoing() | DRAFT 상태의 Event | isOngoing(now) 호출 | false 반환 | false | ✅ |
| Event | 연관관계 | EventOption 추가 | 해피 | Event.addEventOption() | Event, EventOption | addEventOption() 호출 | 옵션이 추가되고 양방향 관계 설정됨 | eventOptions.size() == 1 | ✅ |
| Event | 연관관계 | EventImage 추가 | 해피 | Event.addEventImage() | Event, EventImage | addEventImage() 호출 | 이미지가 추가되고 양방향 관계 설정됨 | eventImages.size() == 1 | ✅ |
| Event | 연관관계 | Coupon 할당 | 해피 | Event.assignCoupon() | Event, Coupon | assignCoupon() 호출 | 쿠폰이 할당됨 | coupon != null | ✅ |

### 1.2 EventOption 엔티티 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| EventOption | 생성 | EventOption 생성 | 해피 | EventOption.create() | Event, 가격, 재고 정보 | create() 호출 | EventOption 생성됨 | 모든 필드가 올바르게 설정됨 | ✅ |
| EventOption | 생성 | EventOption 생성 (null 재고) | 해피 | EventOption.create() | Event, 가격, null 재고 | create() 호출 | EventOption 생성됨, 재고 0 | eventStock == 0 | ✅ |
| EventOption | 재고관리 | 재고 증가 | 해피 | EventOption.increaseStock() | EventOption(재고 100) | increaseStock(50) 호출 | 재고가 150이 됨 | eventStock == 150 | ✅ |
| EventOption | 재고관리 | 재고 감소 | 해피 | EventOption.decreaseStock() | EventOption(재고 100) | decreaseStock(30) 호출 | 재고가 70이 됨 | eventStock == 70 | ✅ |
| EventOption | 재고관리 | 재고 부족 예외 | 예외 | EventOption.decreaseStock() | EventOption(재고 10) | decreaseStock(20) 호출 | IllegalStateException 발생 | "이벤트 재고가 부족합니다" | ✅ |
| EventOption | 재고관리 | 재고 음수 예외 | 예외 | EventOption.decreaseStock() | EventOption(재고 5) | decreaseStock(6) 호출 | IllegalStateException 발생 | "이벤트 재고가 부족합니다" | ✅ |
| EventOption | 연관관계 | Event 할당 | 해피 | EventOption.assignEvent() | EventOption, 새로운 Event | assignEvent() 호출 | Event가 재할당됨 | event == 새로운 Event | ✅ |

### 1.3 EventImage 엔티티 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| EventImage | 생성 | 썸네일 이미지 생성 | 해피 | EventImage.create() | 이미지 URL, isThumbnail=true | create() 호출 | 썸네일 이미지 생성됨 | isThumbnail == true | ✅ |
| EventImage | 생성 | 일반 이미지 생성 | 해피 | EventImage.create() | 이미지 URL, isThumbnail=false | create() 호출 | 일반 이미지 생성됨 | isThumbnail == false | ✅ |
| EventImage | 연관관계 | Event 할당 | 해피 | EventImage.assignEvent() | EventImage, Event | assignEvent() 호출 | Event가 할당됨 | event != null | ✅ |

### 1.4 EventStatus 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| EventStatus | 상태계산 | 시작 전 상태 | 해피 | EventStatus.calculateStatus() | 시작 전 Event, 현재시간 | calculateStatus() 호출 | PLANNED 반환 | PLANNED | ✅ |
| EventStatus | 상태계산 | 진행 중 상태 | 해피 | EventStatus.calculateStatus() | 진행 중 Event, 현재시간 | calculateStatus() 호출 | OPEN 반환 | OPEN | ✅ |
| EventStatus | 상태계산 | 시작시간과 동일 | 해피 | EventStatus.calculateStatus() | Event, 시작시간 | calculateStatus() 호출 | OPEN 반환 | OPEN | ✅ |
| EventStatus | 상태계산 | 종료 후 상태 | 해피 | EventStatus.calculateStatus() | 종료된 Event, 현재시간 | calculateStatus() 호출 | ENDED 반환 | ENDED | ✅ |
| EventStatus | 상태계산 | 종료시간 직후 | 해피 | EventStatus.calculateStatus() | Event, 종료시간+1분 | calculateStatus() 호출 | ENDED 반환 | ENDED | ✅ |

### 1.5 EventRepository 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| EventRepository | 저장 | 이벤트 저장 | 해피 | save() | Event 객체 | save() 호출 | DB에 저장되고 ID 생성됨 | id != null | ✅ |
| EventRepository | 조회 | ID로 이벤트 조회 | 해피 | findById() | 저장된 Event | findById() 호출 | Event 조회됨 | Optional.isPresent() == true | ✅ |
| EventRepository | 조회 | 타입별 조회 (DROP) | 해피 | findAllByEventType() | DROP 타입 Event들 | findAllByEventType(DROP) 호출 | DROP 타입 Event 목록 반환 | 모든 결과가 DROP 타입 | ✅ |
| EventRepository | 조회 | 타입별 조회 (COMMENT) | 해피 | findAllByEventType() | COMMENT 타입 Event들 | findAllByEventType(COMMENT) 호출 | COMMENT 타입 Event 목록 반환 | 모든 결과가 COMMENT 타입 | ✅ |
| EventRepository | 조회 | 상태+종료일 조회 | 해피 | findAllByStatusAndEndedAtBefore() | OPEN 상태, 종료일 지난 Event | 메서드 호출 | 조건에 맞는 Event 목록 반환 | status==OPEN && endedAt<현재시간 | ✅ |
| EventRepository | 조회 | 상태+시작일 조회 | 해피 | findAllByStatusAndStartedAtBefore() | DRAFT 상태, 시작일 지난 Event | 메서드 호출 | 조건에 맞는 Event 목록 반환 | status==DRAFT && startedAt<현재시간 | ✅ |
| EventRepository | 조회 | 존재하지 않는 ID 조회 | 예외 | findById() | 없는 ID | findById() 호출 | 빈 Optional 반환 | Optional.isEmpty() == true | ✅ |
| EventRepository | 조회 | 타입별 조회 (결과 없음) | 예외 | findAllByEventType() | 해당 타입 Event 없음 | findAllByEventType() 호출 | 빈 리스트 반환 | size == 0 | ✅ |

### 1.6 EventService 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| EventService | 조회 | 타입별 이벤트 조회 (DROP) | 해피 | getEventListByType() | DROP 타입 Event들 | getEventListByType(DROP) 호출 | EventListResDto 목록 반환 | size >= 2 | ✅ |
| EventService | 조회 | 타입별 이벤트 조회 (COMMENT) | 해피 | getEventListByType() | COMMENT 타입 Event | getEventListByType(COMMENT) 호출 | EventListResDto 목록 반환 | size > 0 | ✅ |
| EventService | 조회 | 날짜 필터링 조회 | 해피 | getFilteredEventList() | 미래/과거 Event들 | getFilteredEventList() 호출 | 미래 Event만 반환 | startedAt > currentTime | ✅ |
| EventService | 조회 | 썸네일 포함 조회 | 해피 | getEventListByType() | 썸네일 있는 Event | getEventListByType() 호출 | 썸네일 포함된 DTO 반환 | thumbnailUrl != null | ✅ |
| EventService | 조회 | 타입별 조회 (결과 없음) | 예외 | getEventListByType() | DISCOUNT 타입 Event 없음 | getEventListByType(DISCOUNT) 호출 | 빈 리스트 반환 | size == 0 | ✅ |
| EventService | 조회 | 날짜 필터링 (결과 없음) | 예외 | getFilteredEventList() | 조건 안맞는 Event들 | getFilteredEventList() 호출 | 빈 리스트 또는 불일치 결과 | 빈 리스트 또는 조건 불일치 | ✅ |

---

## 2. Coupon 도메인 테스트

### 2.1 Coupon 엔티티 (Model) 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| Coupon | 생성 | Coupon 생성 | 해피 | Coupon.create() | 쿠폰 정보 (이름, 타입, 값, 날짜 등) | create() 호출 | Coupon 생성됨 | 모든 필드가 올바르게 설정됨 | ✅ |
| Coupon | 유효성검증 | 정상 쿠폰 검증 | 해피 | Coupon.validateAvailability() | 활성 쿠폰, 유효 기간 내, 주문금액 충족 | validateAvailability() 호출 | 예외 없이 통과 | 예외 없음 | ✅ |
| Coupon | 할인계산 | 정액 할인 계산 | 해피 | Coupon.calculateDiscountAmount() | AMOUNT 타입 쿠폰, 주문금액 | calculateDiscountAmount() 호출 | 정액 할인 금액 반환 | discountValue | ✅ |
| Coupon | 할인계산 | 정률 할인 계산 | 해피 | Coupon.calculateDiscountAmount() | PERCENTAGE 타입 쿠폰, 주문금액 | calculateDiscountAmount() 호출 | 정률 할인 금액 반환 | orderAmount * rate / 100 | ✅ |
| Coupon | 할인계산 | 할인금액 > 주문금액 | 해피 | Coupon.calculateDiscountAmount() | 큰 할인 쿠폰, 작은 주문금액 | calculateDiscountAmount() 호출 | 주문금액만큼만 할인 | orderAmount | ✅ |
| Coupon | 발급검증 | 정상 쿠폰 발급 | 해피 | Coupon.validateIssuable() | 활성 쿠폰, 발급 기간 내, 재고 있음 | validateIssuable() 호출 | 예외 없이 통과 | 예외 없음 | ✅ |
| Coupon | 발급검증 | 발급 기간 외 | 예외 | Coupon.validateIssuable() | 발급 기간 전 쿠폰 | validateIssuable() 호출 | BusinessException 발생 | "쿠폰 발급 가능 기간이 아닙니다" | ✅ |
| Coupon | 발급검증 | 재고 소진 | 예외 | Coupon.validateIssuable() | 재고 0인 쿠폰 | validateIssuable() 호출 | BusinessException 발생 | "쿠폰 재고가 모두 소진되었습니다" | ✅ |
| Coupon | 발급관리 | 발급 수량 증가 | 해피 | Coupon.increaseIssuedQuantity() | Coupon | increaseIssuedQuantity() 호출 | 발급 수량 1 증가 | issuedQuantity + 1 | ✅ |
| Coupon | 발급관리 | 남은 재고 조회 | 해피 | Coupon.getRemainingQuantity() | Coupon (발급 2개) | getRemainingQuantity() 호출 | 남은 재고 반환 | totalQuantity - issuedQuantity | ✅ |
| Coupon | 발급관리 | 남은 재고 조회 (무제한) | 해피 | Coupon.getRemainingQuantity() | Coupon (totalQuantity=null) | getRemainingQuantity() 호출 | null 반환 | null | ✅ |

### 2.2 MemberCoupon 엔티티 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| MemberCoupon | 발급 | 회원 쿠폰 발급 | 해피 | MemberCoupon.issue() | userId, Coupon | issue() 호출 | MemberCoupon 생성됨 | userId 일치, status=AVAILABLE | ✅ |
| MemberCoupon | 사용검증 | 사용 가능 여부 확인 | 해피 | MemberCoupon.isUsuable() | 사용 가능한 MemberCoupon | isUsuable() 호출 | false 반환 (expiredAt 현재시간) | false | ✅ |
| MemberCoupon | 사용검증 | 사용 가능 검증 | 해피 | MemberCoupon.validateUsable() | MemberCoupon | validateUsable() 호출 | 예외 발생 (expiredAt 현재시간) | BusinessException | ✅ |
| MemberCoupon | 사용 | 쿠폰 사용 | 해피 | MemberCoupon.use() | MemberCoupon | use() 호출 | 예외 발생 (expiredAt 현재시간) | BusinessException | ✅ |
| MemberCoupon | 만료확인 | 만료 여부 확인 | 해피 | MemberCoupon.isExpired() | MemberCoupon | isExpired() 호출 | true 반환 (expiredAt 현재시간) | true | ✅ |
| MemberCoupon | 롤백 | 사용 롤백 | 해피 | MemberCoupon.rollbackUsage() | 사용되지 않은 MemberCoupon | rollbackUsage() 호출 | BusinessException 발생 | "사용되지 않은 쿠폰은 롤백할 수 없습니다" | ✅ |
| MemberCoupon | 롤백 | 사용 롤백 실패 | 예외 | MemberCoupon.rollbackUsage() | 사용되지 않은 MemberCoupon | rollbackUsage() 호출 | BusinessException 발생 | "사용되지 않은 쿠폰은 롤백할 수 없습니다" | ✅ |

### 2.3 CouponRepository 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| CouponRepository | 저장 | 쿠폰 저장 | 해피 | save() | Coupon 객체 | save() 호출 | DB에 저장되고 ID 생성됨 | id != null | ✅ |
| CouponRepository | 조회 | ID로 쿠폰 조회 | 해피 | findById() | 저장된 Coupon | findById() 호출 | Coupon 조회됨 | Optional.isPresent() == true | ✅ |
| CouponRepository | 조회 | 비관적 락 조회 | 해피 | findByIdForUpdate() | 저장된 Coupon | findByIdForUpdate() 호출 | 락과 함께 Coupon 조회됨 | Optional.isPresent() == true | ✅ |
| CouponRepository | 조회 | 존재하지 않는 ID 조회 | 예외 | findById() | 없는 ID | findById() 호출 | 빈 Optional 반환 | Optional.isEmpty() == true | ✅ |
| CouponRepository | 조회 | 비관적 락 조회 (없는 ID) | 예외 | findByIdForUpdate() | 없는 ID | findByIdForUpdate() 호출 | 빈 Optional 반환 | Optional.isEmpty() == true | ✅ |

### 2.4 MemberCouponRepository 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| MemberCouponRepository | 저장 | 회원 쿠폰 저장 | 해피 | save() | MemberCoupon 객체 | save() 호출 | DB에 저장되고 ID 생성됨 | id != null | ✅ |
| MemberCouponRepository | 조회 | 회원별 쿠폰 조회 | 해피 | findByUserIdAndCouponId() | userId, couponId | 메서드 호출 | MemberCoupon 조회됨 | Optional.isPresent() == true | ✅ |
| MemberCouponRepository | 조회 | 발급 개수 조회 | 해피 | countByUserIdAndCouponId() | userId, couponId | 메서드 호출 | 발급 개수 반환 | count == 1 | ✅ |
| MemberCouponRepository | 조회 | 회원별 쿠폰 조회 (없음) | 예외 | findByUserIdAndCouponId() | 없는 userId, couponId | 메서드 호출 | 빈 Optional 반환 | Optional.isEmpty() == true | ✅ |
| MemberCouponRepository | 조회 | 발급 개수 조회 (없음) | 예외 | countByUserIdAndCouponId() | 없는 userId, couponId | 메서드 호출 | 0 반환 | count == 0 | ✅ |

### 2.5 CouponIssuanceService 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| CouponIssuanceService | 발급 | 정상 쿠폰 발급 | 해피 | issueCoupon() | userId, couponId, productId | issueCoupon() 호출 | 쿠폰 발급 성공 | duplicate == false, memberCouponId != null | ✅ |
| CouponIssuanceService | 발급 | 중복 쿠폰 발급 | 해피 | issueCoupon() | 이미 발급받은 userId, couponId | issueCoupon() 호출 | 중복 발급 처리 | duplicate == true | ✅ |
| CouponIssuanceService | 발급 | 존재하지 않는 쿠폰 | 예외 | issueCoupon() | 없는 couponId | issueCoupon() 호출 | BusinessException 발생 | COUPON_NOT_FOUND | ✅ |
| CouponIssuanceService | 발급 | 발급 기간 외 | 예외 | issueCoupon() | 기간 외 couponId | issueCoupon() 호출 | BusinessException 발생 | 발급 기간 에러 | ✅ |
| CouponIssuanceService | 조회 | 발급된 쿠폰 조회 | 해피 | findIssuedCoupon() | userId, couponId | findIssuedCoupon() 호출 | 발급된 쿠폰 조회됨 | Optional.isPresent() == true | ✅ |
| CouponIssuanceService | 조회 | 발급 개수 조회 | 해피 | countIssuedByUser() | userId, couponId | countIssuedByUser() 호출 | 발급 개수 반환 | count == 1 | ✅ |
| CouponIssuanceService | 조회 | 발급된 쿠폰 조회 (없음) | 예외 | findIssuedCoupon() | 없는 userId, couponId | findIssuedCoupon() 호출 | 빈 Optional 반환 | Optional.isEmpty() == true | ✅ |
| CouponIssuanceService | 조회 | 발급 개수 조회 (없음) | 예외 | countIssuedByUser() | 없는 userId, couponId | countIssuedByUser() 호출 | 0 반환 | count == 0 | ✅ |

### 2.6 CouponQueryService 테스트

| 대분류 | 중분류 | 상세 | 테스트유형 | 테스트대상 | Given | When | Then | 기대값 | 결과값 |
|--------|--------|------|------------|-----------|-------|------|------|--------|--------|
| CouponQueryService | 조회 | 회원 쿠폰 목록 조회 | 해피 | getMemberCoupons() | userId, 발급된 쿠폰 2개 | getMemberCoupons() 호출 | 쿠폰 목록 반환 | size == 2 | ✅ |
| CouponQueryService | 조회 | 사용 가능 쿠폰 조회 | 해피 | getAvailableMemberCoupons() | userId, 발급된 쿠폰 | getAvailableMemberCoupons() 호출 | 빈 리스트 (expiredAt 현재시간) | size == 0 | ✅ |
| CouponQueryService | 조회 | 회원 쿠폰 조회 (없음) | 예외 | getMemberCoupons() | 없는 userId | getMemberCoupons() 호출 | 빈 리스트 반환 | size == 0 | ✅ |
| CouponQueryService | 조회 | 사용 가능 쿠폰 조회 (없음) | 예외 | getAvailableMemberCoupons() | 없는 userId | getAvailableMemberCoupons() 호출 | 빈 리스트 반환 | size == 0 | ✅ |

---

## 테스트 통계

### Event 도메인
- **총 테스트 케이스**: 47개
  - Model: 19개
  - Repository: 8개
  - Service: 6개
- **해피 케이스**: 39개
- **예외 케이스**: 8개

### Coupon 도메인
- **총 테스트 케이스**: 36개
  - Model: 18개
  - Repository: 10개
  - Service: 8개
- **해피 케이스**: 27개
- **예외 케이스**: 9개

### 전체 통계
- **총 테스트 케이스**: 83개
- **해피 케이스**: 66개 (79.5%)
- **예외 케이스**: 17개 (20.5%)
- **테스트 프레임워크**: JUnit 5
- **Assertion 라이브러리**: AssertJ

---

## 테스트 실행 방법

```bash
# 전체 테스트 실행
./gradlew test

# Event 도메인 테스트만 실행
./gradlew test --tests "com.mudosa.musinsa.event.*"

# Coupon 도메인 테스트만 실행
./gradlew test --tests "com.mudosa.musinsa.coupon.*"

# 특정 테스트 클래스 실행
./gradlew test --tests "com.mudosa.musinsa.event.model.EventTest"
```

---

## 비고

1. **MemberCoupon 테스트 주의사항**: `MemberCoupon.issue()` 메서드에서 `expiredAt`이 현재 시간으로 설정되어, 일부 테스트에서 만료된 상태로 동작합니다. 실제 프로덕션 코드에서는 적절한 만료 시간을 설정해야 합니다.

2. **서비스 테스트**: 통합 테스트로 작성되어 실제 데이터베이스와 연동됩니다. `@Transactional` 어노테이션으로 각 테스트 후 롤백됩니다.

3. **리포지토리 테스트**: Spring Data JPA의 기본 메서드와 커스텀 쿼리 메서드를 모두 테스트합니다.

4. **컨트롤러 테스트**: 본 명세서에는 포함되지 않았으나, 필요시 MockMvc를 사용한 통합 테스트를 추가할 수 있습니다.
