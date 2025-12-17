# macOS (Apple Silicon) 로컬 튜닝 가이드

로컬에서 높은 동시접속/부하 테스트를 하면 **애플리케이션 설정(Tomcat/Hikari)** 외에 macOS의 **소켓 백로그(somaxconn)** 와 **파일 디스크립터(ulimit / launchctl limit)** 가 병목이 될 수 있습니다.

## 1) 현재 값 확인

- 현재 파일 디스크립터 제한:
  - `ulimit -n`
- launchd(세션) 제한:
  - `launchctl limit maxfiles`
- 소켓 백로그(somaxconn):
  - `sysctl kern.ipc.somaxconn`

## 1.1 macOS 기본값(비교용)

macOS는 “공식 기본값”을 한 줄로 고정해서 제공하진 않고, 버전/환경에 따라 달라질 수 있습니다. 다만 개발 환경에서 자주 보이는 초기값은 아래와 같습니다(반드시 `1)`로 본인 기기 값을 먼저 기록하세요).

- `launchctl limit maxfiles`: 보통 `256 unlimited`
- `ulimit -n`: 보통 `256` (터미널/세션에 따라 다름)
- `kern.ipc.somaxconn`: 보통 `128`

권장 비교 방법:
- 변경 전/후에 `ulimit -n`, `launchctl limit maxfiles`, `sysctl kern.ipc.somaxconn`를 각각 캡처해두고, 같은 부하 조건에서 에러(`Too many open files`, connection refused)와 지연을 비교하세요.

## 2) 임시(현재 터미널 세션)로 올리기

- 파일 디스크립터(현재 쉘만):
  - `ulimit -n 65536`
- 소켓 백로그(재부팅 시 원복):
  - `sudo sysctl -w kern.ipc.somaxconn=2048`

## 3) 영구 반영(권장)

### 3.1 maxfiles (파일 디스크립터) 영구 반영

macOS는 일반적으로 `ulimit`만으로는 재부팅/재로그인 후 유지되지 않습니다. 아래는 많이 쓰는 방식입니다.

- 시스템 전역(launchd) 제한 확인/설정:
  - 확인: `launchctl limit maxfiles`
  - 설정(재부팅/재로그인 필요할 수 있음):
    - `sudo launchctl limit maxfiles 65536 200000`

환경/버전에 따라 `launchctl limit`이 세션별로만 적용되는 경우가 있어, 필요하면 `LaunchDaemons` 플리스트로 고정하세요.

- 예시(플리스트) 경로:
  - `/Library/LaunchDaemons/limit.maxfiles.plist`
- 예시 내용(참고):
  - `launchd`에서 `launchctl limit maxfiles`를 실행하는 형태로 구성

### 3.2 somaxconn 영구 반영

macOS는 Linux처럼 `/etc/sysctl.conf`가 기본 활성화가 아닐 수 있어, 영구 반영은 환경별로 차이가 있습니다.

- 우선 권장: 로컬 부하 테스트 전에만 `sudo sysctl -w kern.ipc.somaxconn=2048` 실행
- 필요 시: LaunchDaemon으로 `sysctl` 적용 스크립트를 부팅 시 실행

## 4) 애플리케이션 측 설정(이 레포)

- `src/main/resources/application-dev.yml`에서 아래 값들을 환경변수로 조절할 수 있습니다.
  - `DB_POOL_MAX`, `DB_POOL_MIN_IDLE`
  - `TOMCAT_THREADS_MAX`, `TOMCAT_ACCEPT_COUNT`, `TOMCAT_MAX_CONNECTIONS` 등
  - `TRACING_SAMPLING_PROBABILITY`, `OTLP_TRACES_ENDPOINT`

## 5) 체크 포인트

- IntelliJ에서 실행한다면, **IntelliJ(및 IntelliJ가 띄우는 Gradle/Java 프로세스)는 launchd의 기본 limit을 상속**합니다.
  - `ulimit -n`이 터미널에서 높게 찍혀도, `launchctl limit maxfiles`가 `256 unlimited`처럼 낮으면 IDE로 띄운 JVM은 256을 물고 시작할 수 있습니다.
  - 해결: `sudo launchctl limit maxfiles 65536 unlimited` 실행 후 **재로그인(또는 재부팅)** 하고 IntelliJ를 다시 실행하세요.
- 적용 확인(실행 중인 JVM PID 기준):
  - PID 확인: `ps -ax | rg -n \"java.*musinsa|GradleDaemon\"`
  - 열린 FD 확인: `lsof -p <PID> | wc -l`
- `max-connections`/`threads`를 너무 크게 올리면, 로컬 MySQL/Redis/Elasticsearch가 먼저 한계에 닿을 수 있습니다.
- 실측 기준으로 한 번에 크게 올리기보다, `ulimit -n`/`somaxconn`를 먼저 올린 뒤 단계적으로 조정하는 걸 권장합니다.
