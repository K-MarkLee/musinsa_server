# OpenSearch 배포: 제품 헤더 검증을 우회한 클라이언트 구성

[English](../../en/troubleshooting/opensearch-deployment.md) · [README](../../../README.ko.md)

---

## 문제

로컬 Elasticsearch에서 동작하던 검색을 AWS OpenSearch에 연결하자 Elastic Java Client가 응답을 거부했습니다.<br>
클라이언트는 `X-Elastic-Product` 헤더로 Elasticsearch 응답인지 확인하는데, OpenSearch 응답에는 이 헤더가 없어 예외가 발생했습니다.

---

## 대안과 선택

| 대안 | 바꿔야 하는 범위 |
| --- | --- |
| 검색 엔진 변경 | 배포 환경의 검색 엔진과 데이터 구성 |
| OpenSearch 전용 클라이언트로 전환 | 클라이언트 의존성과 검색 연동 코드 전체 |
| 응답 헤더 추가와 호환 헤더 적용 | 연결 설정만 바꾸고 기존 검색 코드는 유지 |

배포를 진행하면서 기존 검색 코드를 그대로 쓰기 위해, 연결 설정에서 응답 헤더를 추가하고 ES 7 호환 요청 헤더를 적용했습니다.

---

## 조치

요청에는 ES 7 호환 헤더를 기본으로 붙였습니다.

```http
Accept: application/vnd.elasticsearch+json;compatible-with=7
Content-Type: application/json;compatible-with=7
```

응답에 제품 헤더가 없으면 인터셉터가 추가하도록 했습니다.

```java
HttpResponseInterceptor productHeaderInjector = (response, context) -> {
    if (!response.containsHeader("X-Elastic-Product")) {
        response.addHeader("X-Elastic-Product", "Elasticsearch");
    }
};
httpClientBuilder.addInterceptorLast(productHeaderInjector);
```

이 RestClient를 Basic Auth와 함께 기존 `ElasticsearchClient`에 연결해, 검색 코드를 바꾸지 않고 배포 환경에서 검색을 사용할 수 있었습니다.

---

## 선택과 트레이드오프

- **버전 관리 책임**: 검색 코드를 그대로 쓰는 대신, 서버와 클라이언트의 버전 차이를 연결 설정에서 직접 맞춰야 합니다.
  OpenSearch를 계속 쓴다면 전용 클라이언트로 바꿔 서버와 클라이언트를 맞추는 것이 장기적으로 맞습니다.<br>
