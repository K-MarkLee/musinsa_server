
# 🔍 검색 서비스 고도화 보고서 (Search Optimization Report)


## 0. 프로젝트 화면
유튜브 링크 : https://youtu.be/oC8-prv8Qdo

<img src="./docs/images/musinsang-gif.webp" alt="gif" width="720" />



---

## 1. 프로젝트 개요 (Overview)
본 프로젝트는 단순한 기능 구현에서 시작하여, 대용량 트래픽과 데이터 상황을 가정하고 점진적으로 시스템을 고도화한 **2개월간의 엔지니어링 기록**입니다.

### 프로젝트 여정 (Evolution Journey)

#### Phase 1: 기능 구현 (MVP)
- **목표**: 이커머스의 핵심 도메인인 “상품”을 중심으로 요구사항을 정리하고, MVP 수준의 CRUD를 빠르게 구현.
- **요구사항 명세**: 상품 등록/수정/삭제, 상품 조회(목록/상세), 검색/필터, 페이징 등.

#### Phase 2: 리팩토링 및 테스트 (Stability)
- **목표**: 테스트 기반 리팩토링으로 구조적 안정성 확보.
- **테스트 전략**:
  - Unit Test로 도메인/서비스 로직을 빠르게 검증.
  - Integration Test로 API/DB 연동 및 주요 플로우를 검증.
- **리팩토링 포인트**:
  - Controller-Service-Repository 계층 책임을 명확히 분리.
  - **SRP(단일 책임 원칙) + CQRS 관점 분리**: 상품 관리(Command)와 조회/검색(Query) 책임을 분리해 변경 영향도를 축소.

#### Phase 3: 대용량 데이터 챌린지 (Scalability)
- **상황**: 네이버 쇼핑 API를 활용해 상품 데이터를 대량 적재하고(약 1,000만 건), 로컬 환경에서 부하 테스트를 수행.
- **목표**: 병목을 찾고, 성능 개선 포인트를 “지표 기반”으로 확인(Grafana, K6, Prometheus).
- **진행**:
  - 대용량 데이터 환경에서 기존 `LIKE` 기반 검색의 한계를 확인하고 검색 전용 엔진(Elasticsearch) 도입.
  - 로컬 리소스(디바이스) 한계로 인해, AWS 배포까지 완료했으나 AWS 환경에서의 추가 튜닝/개선은 시간 관계상 진행하지 못함.


---

## 2. 기술 스택 (Tech Stack)
<img src="./docs/images/Skills.png" alt="Skills" width="720" />


---

## 3. 담당 기능 (Responsibilities)
### 상품 조회 (List)
- 정렬: 기본 정렬, 가격 낮은 순, 가격 높은 순
- 필터: 카테고리 기반 필터링

### 상품 상세 조회 (Detail)
- 검색 결과에서 선택한 옵션(색상/사이즈 등)이 선택된 상태로 상세 페이지 진입/조회

### 상품 검색 (Search)
- 검색 대상: 브랜드명, 카테고리, 상품명, 색상, 사이즈
- 추가 기능: 필터링 및 정렬 지원

---

## 4. 성능 개선치

### 단순 조회(List) 기준

#### Performance
- Summary: 30s → 275ms

<img src="./docs/images/list-30s.png" alt="List baseline 30s" width="720" />

<img src="./docs/images/list-no-monitor.png" alt="List no monitor" width="720" />

<br>

<br>

### 상세 조회(Detail) 기준

#### Performance
- Summary : 30s -> 723ms

<img src="./docs/images/detail-835ms.png" alt="Detail optimized 835ms" width="720" />

<img src="./docs/images/detail-no-monitor.png" alt="Detail no monitor" width="720" />

<br>

<br>


### 검색(Search) 기준 (Elasticsearch)

#### Performance
- Summary : 5s -> 2.3s

<img src="./docs/images/es-5s.png" alt="ES search 5s" width="720" />

<img src="./docs/images/es-no-monitor.png" alt="ES no monitor" width="720" />

<br>

<br>


---

## 5. WIKI 및 참고 자료
자세한 설명과 트러블슈팅은 WIKI에 정리했습니다. 아래의 링크를 참조해주세요.


[WIKI 바로가기](https://github.com/k9want/sellect-ecommerce-platform/wiki)
