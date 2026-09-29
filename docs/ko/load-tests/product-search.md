# 검색 고부하 결과

[English](../../en/load-tests/product-search.md) · [README](../../../README.ko.md) · [공통 측정 환경](../environment.md)

`스커트`·`블랙 스커트`·`나이키`·`나이키 후드`의 첫 10개를 균등 조회했다.<br>
250 RPS 유지 후 500 RPS 증가 중 중단, HTTP 실패 0건·drop 179건.<br>
ES HTTP 전체/호스트별 상한은 30/10이었다.<br>
500 RPS 유지와 이후 단계는 완료하지 못했다.

검색 k6

![검색 k6](../../images/evidence/load-search-1.png)

---

## 측정 조건

2026년 9월 23일 최종 실행 1회다.<br>
7분 예정 시나리오는 10 RPS 워밍업부터 50·100·250·500·750·1,000 RPS로 증가하며, 마지막 1,000 RPS는 120초 유지한다.<br>
VU 상한 200, HTTP timeout 5초, Hikari 상한 10이다.<br>
검색은 ES HTTP 전체/호스트별 상한 30/10에서 조기 중단됐다.<br>
다른 API와 동시에 호출한 부하는 아니다.

---

## 전체 실행 응답

| 응답 표본 수 | 평균 | p95 | 최대 |
| ---: | ---: | ---: | ---: |
| 21,728 | 44.03ms | 279.62ms | 586.26ms |

전체는 워밍업·증가 구간을 포함한다.<br>
아래 단계별 표와 그래프의 선택 범위 요약값을 구분한다.

---

## 부하 단계별 결과

| 구간 | 응답 표본 수 | 평균 | p95 |
| --- | --- | --- | --- |
| 50 RPS 유지 | 1,500 | 16.91ms | 41.13ms |
| 100 RPS 유지 | 3,000 | 15.98ms | 39.95ms |
| 250 RPS 유지 | 7,500 | 15.39ms | 40.53ms |
| 250 → 500 RPS 증가 · 중단 | 5,229 | 132.05ms | 504.68ms |

---

## 연결·자원 관측

Spring Boot · Hikari 연결 관측

![Spring Boot · Hikari 연결 관측](../../images/evidence/load-search-2.png)

아래 최대값은 같은 실행의 1초 간격 조회이며 발생 시각은 서로 다를 수 있다.<br>
시스템 CPU는 MySQL 단독 CPU가 아니다.

ES HTTP pool

![ES HTTP pool](../../images/evidence/load-search-3.png)

| 관측 항목 | 결과 |
| --- | --- |
| 전체 / 호스트별 연결 상한 | 30 / 10 |
| leased 최대 · 첨부 화면 | 10 |
| pending 최대 · 첨부 화면 | 188 |
| pending 최대 · 1초 간격 원본 조회 | 189 |
| Tomcat busy 최대 · 원본 조회 | 199 |
| JVM / 시스템 CPU 최대 · 원본 조회 | 11.70% / 95.99% |

원본 메트릭은 실행 구간과 종료 직후 정리 구간을 1초 간격으로 확인했다.<br>
첨부 그래프는 더 성긴 간격으로 조회돼 짧은 pending 피크가 표시되지 않을 수 있다.<br>
화면의 pending=0과 표의 실행 내 최대값은 구분하며, 각 지표의 최대값이 같은 시각에 발생했다는 뜻도 아니다.

---

## 요청 트레이스 표본

검색 Jaeger

![검색 Jaeger](../../images/evidence/load-search-4.png)

| 구간·태그 | 첨부 표본의 시간 |
| --- | --- |
| HTTP 전체 | 554.81ms |
| ES 검색 호출 스팬 | 554.39ms |
| es.client_ms | 554ms |
| es.took_ms | 65ms |
| es.outside_took_ms | 489ms |

Hikari pending은 0이었지만 ES HTTP leased가 호스트별 상한 10에 도달했고 pending이 증가했다.<br>
`outside_took_ms=489` 전체를 풀 대기로 단정하지 않는다.<br>
k6 화면의 평균 44.10ms·p95 281.84ms는 선택 범위가 달라 전체 집계와 소폭 다르다.<br>
후속 풀 100·150 비교 및 CPU 관측은 [검색 병목 조사](../troubleshooting/search-capacity.md)의 별도 실행이다.

---

## 결과 해석과 측정 범위

고정된 입력을 반복한 단일 API 시험이며 캐시·JIT 예열과 로컬 자원 공유의 영향을 포함한다.<br>
높은 부하 단계에서 지연이 줄어든 것을 부하 증가 자체의 효과로 해석하지 않는다.<br>
HTTP 200 확인은 응답 내용 전체의 정확성 검증과 다르다.<br>
한 트레이스의 시간은 전체 평균·p95를 대신하지 않으며 DB 호출 스팬도 순수 SQL 시간만을 뜻하지 않는다.

Hikari active와 pending은 모두 0이었고 연결 timeout 증가도 없었다.<br>
종료 후 ES available 10·leased 0은 연결이 반환된 상태다.<br>
`took`은 ES 서버 측 경과 시간이고 `outside=client−took`에는 연결 대기·전송·응답 처리가 섞인다.<br>
이 실행의 우선 확인 대상은 호스트별 연결 상한과 그 뒤의 검색 용량이며, 높은 시스템 CPU를 ES 단독 사용률로 바꾸어 해석하지 않는다.

---

## 현재 확인한 부하 범위와 한계

30/10 연결 설정에서 250 RPS 유지 후 500 RPS 증가 중 중단됐다.<br>
따라서 250 RPS를 정확한 최대치로 확정하지 않는다.<br>
연결 풀 확대 후의 진단도 목표 부하를 완주하지 못했으며, [검색 병목 조사](../troubleshooting/search-capacity.md)에 별도로 기록했다.<br>
쿼리·문서 모델 변경 전 추가 최대치 측정은 이번 작업에 포함하지 않았다.
