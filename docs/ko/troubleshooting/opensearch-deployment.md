# OpenSearch 배포: 제품 헤더 검증을 우회한 클라이언트 구성

[English](../../en/troubleshooting/opensearch-deployment.md) · [README](../../../README.ko.md) · [공통 측정 환경](../environment.md)

---

## 문제: 로컬 검색이 배포 환경에서 실패

로컬 Elasticsearch에서 동작하던 검색을 AWS OpenSearch에 연결하자 Elastic Java Client가 응답을 거부했다.<br>
클라이언트는 `X-Elastic-Product` 헤더로 Elasticsearch 응답인지 확인하는데, OpenSearch 응답에는 이 헤더가 없어 제품 검증 단계에서 예외가 발생했다.

---

## 대안과 선택

| 대안 | 적용 범위 |
| --- | --- |
| 서버 엔진 변경 | 배포 검색 엔진과 데이터 구성을 변경해야 한다. |
| OpenSearch 전용 클라이언트로 전환 | 클라이언트 의존성과 검색 연동 코드를 함께 변경해야 한다. |
| 응답 헤더 인터셉터와 호환 헤더 적용 | 기존 검색 코드를 유지하고 연결 설정에서 제품 검증 문제를 해결할 수 있다. |

배포를 진행하기 위해 커스텀 RestClient에 응답 헤더 인터셉터와 ES 7 호환 요청 헤더를 적용했다.

---

## 적용한 우회

요청에는 다음 기본 헤더를 설정했다.

```http
Accept: application/vnd.elasticsearch+json;compatible-with=7
Content-Type: application/json;compatible-with=7
```

응답에 제품 헤더가 없을 때 인터셉터가 추가하도록 구성했다.

```java
HttpResponseInterceptor productHeaderInjector = (response, context) -> {
    if (!response.containsHeader("X-Elastic-Product")) {
        response.addHeader("X-Elastic-Product", "Elasticsearch");
    }
};
httpClientBuilder.addInterceptorLast(productHeaderInjector);
```

Basic Auth와 함께 이 RestClient를 `RestClientTransport`에 연결해 기존 `ElasticsearchClient`에서 사용했다.<br>
제품 검증에서 막히던 연동을 연결 설정 수준에서 우회해 검색 기능을 이어 갔다.

---

## 선택 이유와 트레이드오프

검색 질의와 응답 매핑을 전환하지 않고 배포 문제를 해결할 수 있다는 점을 우선했다.<br>
그 대신 서버와 클라이언트의 버전 차이를 연결 계층에서 직접 관리하는 책임이 생겼다.

장기적으로 OpenSearch를 유지한다면 전용 클라이언트로 전환해 서버와 클라이언트를 맞추는 방향이다.<br>
이번 우회는 제품 헤더 검증과 요청 형식을 맞추는 조치이며, 엔진 자체를 Elasticsearch로 바꾸는 작업은 아니다.<br>
[OpenSearch 클라이언트 안내](https://docs.opensearch.org/latest/clients/)
