# Toss Payments Fake Server

부하 테스트를 위한 토스페이먼츠 API 모킹 서버입니다.

## 서버 정보
- **포트**: 8081
- **Base URL**: `http://localhost:8081`

## API 명세

### 1. 결제 승인 API

실제 토스페이먼츠 URL과 유사하게 구현되었습니다.

**Endpoint**
```
POST /v1/payments/confirm
```

**Request Body**
```json
{
  "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
  "orderId": "ORDER_123456",
  "amount": 10000
}
```

**필수 파라미터**
- `paymentKey` (String): 결제 키 - 필수
- `orderId` (String): 주문 ID - 필수
- `amount` (Long): 결제 금액 - 필수, 0보다 큰 값

**Success Response (200 OK)**
```json
{
  "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
  "status": "DONE",
  "lastTransactionKey": "a1b2c3d4e5f6g7h8i9j0",
  "orderId": "ORDER_123456",
  "approvedAt": "2024-01-15T10:30:00+09:00",
  "method": "CARD",
  "totalAmount": 10000
}
```

**Failure Response (400 Bad Request)**
```json
{
  "code": "INVALID_REQUEST",
  "message": "paymentKey는 필수 값입니다."
}
```

### 2. 결제 취소 API

**Endpoint**
```
POST /v1/payments/{paymentKey}/cancel
```

**Path Parameter**
- `paymentKey` (String): 결제 키 - 필수, 최대 200자

**Request Body**
```json
{
  "cancelReason": "고객 변심"
}
```

**필수 파라미터**
- `cancelReason` (String): 취소 사유 - 필수

**Success Response (200 OK)**
```json
{
  "paymentKey": "5EnNZRJGvaBX7zk2yd8ydw26XvwXkLrx9POLqKQjmAw4b0e1",
  "orderId": "ORDER_a1b2c3d4",
  "status": "CANCELED",
  "canceledAt": "2024-01-15T10:35:00+09:00",
  "cancelReason": "고객 변심",
  "cancelAmount": 10000
}
```

**Failure Response (400 Bad Request)**
```json
{
  "code": "INVALID_REQUEST",
  "message": "cancelReason은 필수 값입니다."
}
```

### 3. 타임아웃 테스트 API

부하 테스트 중 타임아웃 시나리오를 테스트하기 위한 엔드포인트입니다.

**Endpoint**
```
POST /v1/payments/confirm/timeout
```

**특징**
- 15초 지연 후 408 Timeout 응답 반환
- Request Body는 일반 confirm API와 동일

**Response (408 Request Timeout)**
```json
{
  "code": "TIMEOUT",
  "message": "결제 승인 요청 시간이 초과되었습니다."
}
```

### 4. Health Check

**Endpoint**
```
GET /health
```

**Response**
```
OK
```

## 성능 특성

- **PG 처리 지연 시뮬레이션**: 모든 정상 요청은 300~800ms의 랜덤 지연이 발생합니다.
- **타임아웃 테스트**: `/confirm/timeout` 엔드포인트는 15초 지연 후 응답합니다.

## 실행 방법

```bash
# Gradle 빌드
./gradlew build

# 서버 실행
./gradlew bootRun
```

서버가 시작되면 `http://localhost:8081`로 접근할 수 있습니다.

## cURL 예제

### 결제 승인
```bash
curl -X POST http://localhost:8081/v1/payments/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "paymentKey": "test_payment_key_123",
    "orderId": "ORDER_20240115_001",
    "amount": 15000
  }'
```

### 결제 취소
```bash
curl -X POST http://localhost:8081/v1/payments/test_payment_key_123/cancel \
  -H "Content-Type: application/json" \
  -d '{
    "cancelReason": "고객 요청으로 인한 취소"
  }'
```

### Health Check
```bash
curl http://localhost:8081/health
```

## 부하 테스트 시나리오

이 서버는 다음과 같은 부하 테스트 시나리오를 지원합니다:

1. **정상 플로우**: `/v1/payments/confirm` 호출
2. **취소 플로우**: 승인 후 `/v1/payments/{paymentKey}/cancel` 호출
3. **타임아웃 시나리오**: `/v1/payments/confirm/timeout` 호출
4. **실패 시나리오**: 필수 파라미터 누락

## 토스페이먼츠 실제 API와의 차이점

- 실제 API: `https://api.tosspayments.com/v1/payments/...`
- Fake Server: `http://localhost:8081/v1/payments/...`

부하 테스트 시 환경 변수나 설정을 통해 URL만 변경하면 됩니다.
