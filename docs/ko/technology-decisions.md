# 기술 선택 이유

[English](../en/technology-decisions.md) · [README](../../README.ko.md)

---

## QueryDSL — JPA에서 조회를 작성하는 방식의 선택

### 요구사항

카테고리·성별·브랜드·가격 정렬이 선택적으로 조합되고, 정렬에 맞는 커서 조건과 목록 DTO 조회가 필요했다.

| 비교 대상 | 적합한 경우 | 이 프로젝트에서 고려할 점 |
| --- | --- | --- |
| Spring Data JPA 파생 메서드 | 조건이 단순하고 조회 형태가 고정된 경우 | 선택 조건의 조합이 늘면 메서드 수와 이름 관리가 복잡해질 수 있음 |
| `@Query`·JPQL | 고정된 조인·조회 구조를 명시할 때 | 선택 조건·정렬·커서의 조합을 문자열 질의에서 관리해야 함 |
| Specification | JPA Criteria 기반 조건을 조합·재사용할 때 | 조건 조합뿐 아니라 DTO 조회·정렬까지 표현하는 코드의 가독성을 비교할 대상 |
| QueryDSL — 적용 | 조건·정렬·조인·프로젝션을 Java 코드로 구성할 때 | Q타입 생성과 추가 의존성 관리 필요 |

### 선택 이유

JPA를 유지하면서 필터·정렬·커서 조건을 작은 표현식으로 나눠 재사용하고, 목록 DTO 조회까지 한 흐름으로 작성하려고 QueryDSL을 사용했다.<br>
비교 대상은 JPA 자체가 아니라 JPA 위에서 쿼리를 작성하는 방식이다.

### 트레이드오프

빌드에서 Q타입을 생성·관리해야 하며, QueryDSL이 효율적인 SQL을 자동으로 보장하지는 않는다.<br>
조회량·실행 계획·인덱스는 별도로 확인했으며, 다른 작성 방식보다 실행 속도가 빠르다는 비교는 하지 않았다.

[목록 DTO](improvements/list-thumbnail-dto.md) · [필터 인덱스](improvements/filter-indexes.md) · [Spring Data JPA 조회 방식](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html) · [Specification](https://docs.spring.io/spring-data/jpa/reference/jpa/specifications.html)

---

## MySQL — 관계형 데이터 관리와 팀의 사용 경험

### 요구사항

상품·옵션·재고·브랜드 관계를 관리하고, 필터·정렬·페이지네이션을 SQL과 인덱스로 처리해야 했다.

| 비교 기준 | MySQL — 적용 | PostgreSQL — 대안 |
| --- | --- | --- |
| 관계형 조회 | 조인·정렬·복합 인덱스로 현재 조회 구현 | 같은 관계형 조회 요구를 검토할 수 있는 대안 |
| 실제 질의의 비용 | 현재 데이터에서 실행 계획·인덱스·연결 대기 관측 | 같은 데이터·질의·환경으로 별도 비교 필요 |
| 개발·운영 적합성 | 팀의 사용 경험과 기존 JPA·Docker·관측 구성 활용 | 팀의 숙련도와 새로 익힐 구성·배포·운영 작업을 함께 고려 |

### 선택 이유

필요한 관계형 조회를 구현할 수 있고, 팀의 사용 경험을 활용해 도입·운영의 학습 부담을 줄일 수 있어 MySQL을 선택했다.<br>
기존 JPA·Docker 구성에서 조회 구조와 인덱스를 개선하고, 실행 계획과 연결 대기로 그 비용을 확인했다.

### 트레이드오프

PostgreSQL도 현재 요구에 필요한 관계형 조회와 [여러 인덱스 방식](https://www.postgresql.org/docs/current/indexes.html)을 제공하므로, 이 선택은 DB 간 성능 우위를 검증한 결과가 아니다.<br>
MySQL에서 확인한 실행 계획과 튜닝 결과를 다른 DB에 그대로 적용할 수는 없으며, DB를 바꾸면 실제 질의를 다시 검증해야 한다.<br>
현재 측정은 MySQL 안에서 조회 구조와 설정을 바꾼 결과이고, 대표값 갱신과 쓰기 정합성은 후속 검증 범위다.

[대표 가격](improvements/list-price.md) · [필터 실행 계획](improvements/filter-indexes.md) · [연결 풀 비교](troubleshooting/hikari.md)

---

## Redis — 로컬 캐시·DB 직접 조회와 비교

### 요구사항

상세 응답에서 여러 옵션이 반복해서 사용하는 색상·사이즈 사전을 읽어야 했다.<br>
사전은 60개이며, 가격·재고와 전체 상품 응답은 캐시 대상에서 제외했다.

| 비교 대상 | 얻는 점 | 감수할 비용 |
| --- | --- | --- |
| 로컬 캐시 | 사전 조회의 네트워크 왕복 제거 | 인스턴스별 적재·갱신·무효화와 값의 차이 관리 |
| DB 조인·일괄 조회 | 원본과 함께 읽고 별도 캐시 의존성 감소 | DB 왕복·조인·문자열 중복 전송 비용.<br>버퍼풀에 있어도 이 비용은 확인 필요 |
| Redis — 적용 | 여러 인스턴스가 참조할 공통 사전과 `multiGet` 일괄 조회 | 네트워크·직렬화, Redis 가용성과 초기화·갱신 정책 |

### 선택 이유

반복되는 사전 조회를 DB 관계 탐색에서 분리하고, 필요한 값을 `multiGet`으로 묶어 읽으려고 Redis를 사용했다.<br>
인스턴스마다 사본을 두는 로컬 캐시와 달리 공통 사전을 한곳에서 관리할 수 있다.<br>
다중 인스턴스에서 공유할 수 있다는 것은 설계상의 이점이며, 현재 측정에서 그 운영 효과까지 검증한 것은 아니다.

### 트레이드오프

Redis 조회에도 네트워크 왕복·직렬화가 필요하며, 캐시 갱신과 장애 처리를 추가로 관리해야 한다.<br>
버퍼풀은 DB 페이지를 메모리에 보관하는 기능이므로, DB 직접 조회의 대안 안에서 비교한다.<br>
현재처럼 작은 사전은 로컬 메모리에 두거나 DB에서 일괄 조회하는 편이 더 단순할 수 있다.

조회 구조와 Redis 사용을 함께 바꿨으므로 상세 개선율 전체를 Redis의 효과로 설명하지 않는다.<br>
대안 간 성능 비교와 사전 갱신·누락·장애 정책은 후속 과제다.

[상세 조회와 캐시 범위](improvements/product-detail.md) · [대안별 검증 계획](future-work.md)

---

## Elasticsearch — LIKE·MySQL FULLTEXT와 비교

### 요구사항

`블랙 스커트`처럼 상품명·색상·종류에 흩어진 의도를 찾고, 동의어와 필드별 가중치로 관련도 순위를 조절해야 했다.

| 비교 대상 | 검색 요구와의 관계 | 구현·운영 비용 |
| --- | --- | --- |
| 기존 LIKE | 상품명 연속 문자열 검색·ID순 반환 | 기존 DB만 사용.<br>현재 방식에서 토큰·다중 필드·관련도 요구를 추가 구현해야 함 |
| MySQL FULLTEXT | CJK ngram·관련도 지원 | 검색용 인덱스와 질의 설계 필요.<br>요구하는 동의어·옵션 조합·가중치의 구현 범위를 확인할 대안 |
| Elasticsearch — 적용 | 형태소·동의어·다중 필드·가중치를 검색 모델로 구성 | 별도 서버·색인 운영, DB 변경 전파·재색인·실패 복구 |

### 선택 이유

검색어의 포함 여부를 넘어 형태소·동의어·다중 필드 매칭을 구성하고, 필수 매칭 조건과 관련도 순위를 따로 조절하려고 ES를 사용했다.<br>
옵션마다 문서를 만들어 실제 색상·사이즈 조합을 유지하고, `productId` collapse로 같은 상품의 중복 결과를 묶어 반환했다.

### 트레이드오프

별도 검색 서버와 색인을 운영해야 하며, DB 변경 전파·재색인·실패 복구를 설계해야 한다.<br>
옵션 문서마다 상품 정보가 중복되고, 검색 시 상품별로 결과를 묶는 비용과 페이지 이동 제약이 생긴다.<br>
상품 중심 nested 모델은 옵션 조합을 유지하면서 문서 구조를 바꿀 수 있는 후속 대안이며, 조회·갱신 비용을 함께 비교할 예정이다.

LIKE 전후는 검색 기능이 바뀐 비교이므로 응답시간만으로 도입 이유를 설명하지 않는다.<br>
FULLTEXT는 사후 대안 검토이며, 품질·성능을 직접 비교하지 않았다.

[검색 사례](improvements/product-search.md) · [검색 페이지네이션](troubleshooting/search-pagination.md) · [FULLTEXT 관련도](https://dev.mysql.com/doc/refman/8.4/en/fulltext-natural-language.html) · [CJK ngram](https://dev.mysql.com/doc/refman/8.4/en/fulltext-search-ngram.html)

---

## 부하 테스트·모니터링 — 부하와 자원, 요청 경로를 함께 보기 위해

### 요구사항

같은 입력과 부하를 반복하고, 응답시간이 늘어나는 시점의 자원 상태와 요청 내부 대기 구간을 함께 확인해야 했다.

### 선택 이유

로그는 개별 사건을, 프로파일러는 메서드 실행과 CPU 사용 등을 조사하는 데 도움이 된다.<br>
여기에 반복 가능한 부하와 시간에 따른 자원 변화, 요청별 스팬을 연결하기 위해 다음 도구를 함께 사용했다.

| 도구 | 선택 이유와 이 프로젝트에서의 역할 | 적용 사례 |
| --- | --- | --- |
| k6 | JavaScript로 입력·부하·판정 기준을 기록하고, 1 VU 비교와 목표 호출률 부하에서 지연·HTTP 실패·drop 확인 | [시나리오와 판정 기준](environment.md) |
| Prometheus | 메트릭을 시계열로 수집해 Hikari active/pending, Tomcat busy, ES HTTP 풀의 변화를 부하 단계와 연결 | [연결 풀 조사](troubleshooting/hikari.md) |
| Grafana | 처리량·지연·자원 지표를 대시보드에서 같은 시간축으로 비교해 조사할 구간 선택 | [고부하 결과](../../README.ko.md#고부하-결과) |
| Jaeger | 요청을 스팬으로 나눠 서비스 진입·DB 조회·DTO 조립 중 시간이 걸리는 구간 확인 | [상세 지연 구간 분리](troubleshooting/detail-isolation.md) |
| ES Profile | 검색 질의의 연산별 실행 구성을 확인해 비용이 큰 연산 조사 | [검색 질의 비용](troubleshooting/search-capacity.md) |
| ES hot threads | 부하 중 스레드 스택을 확인해 CPU와 검색 큐 증가 시 실행 중인 작업 조사 | [검색 CPU·큐 조사](troubleshooting/search-capacity.md) |

### 트레이드오프

수집·저장·추적에도 자원이 들며, 같은 장비에서 실행한 관측 도구가 부하 결과에 영향을 줄 수 있다.<br>
DEBUG 로그와 불필요한 추적을 줄이고, ES Profile은 일반 부하 측정과 분리했다.

지표가 함께 변한다는 사실은 원인 후보를 찾는 근거이며, 설정을 바꾼 실험으로 확인해야 한다.<br>
개별 트레이스와 hot threads 표본은 전체 요청을 대표하지 않으므로, 전체 지연과 유지 구간 통계는 부하 측정값과 집계 기준을 따랐다.

[관측 도구의 비용](troubleshooting/observability-overhead.md) · [Prometheus 수집 구조](https://prometheus.io/docs/introduction/overview/) · [Jaeger 개념](https://www.jaegertracing.io/docs/1.76/architecture/) · [ES Profile 범위와 비용](https://www.elastic.co/docs/reference/elasticsearch/rest-apis/search-profile)
