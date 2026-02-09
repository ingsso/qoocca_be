# 오늘 성능 테스트 정리 (2026-02-09)

## 1) 목표
- 로컬 환경에서 k6 + Spring Boot Actuator/Micrometer + Prometheus + Grafana + MySQL Exporter 기반 성능 측정
- 출결/수납 조회 중심 운영 시나리오 테스트
- 다중 유저 + 다중 학원 동시 조회 성능 확인

## 2) 환경 구성
- Docker 서비스: mysql, redis, prometheus, grafana(3100), mysqld_exporter, k6
- API: http://localhost:8080
- Grafana: http://localhost:3100

## 3) 주요 변경 및 보완
### 3-1. k6 스크립트 개선
- performance/k6/attendance_receipt_mixed.js
  - USERS_CSV 기반 VU별 다른 계정 로그인
  - 유저 학원 없으면 자동 생성 + 관리자 승인
  - 유저별 학원/클래스/학생 자동 조회·생성
- performance/k6/users.csv
  - user1~user50 생성 완료

### 3-2. 사전 시드 스크립트 추가
- performance/k6/seed_multi_academy.js
  - 유저별 학원 생성 → 자동 승인 → 클래스 생성 → 학생 N명 생성

### 3-3. Grafana 대시보드 보강
- Hikari Pending Connections 패널 추가
  - hikaricp_connections_pending{application="qoocca-db"}

## 4) Redis 캐시 에러 처리
- 오류: GenericJackson2JsonRedisSerializer 역직렬화 오류 반복
- 해결:
```powershell
 docker compose exec redis redis-cli FLUSHALL
```

## 5) 실행 커맨드 (메인 테스트)
```powershell
 docker compose exec k6 k6 run `
  -e BASE_URL=http://host.docker.internal:8080 `
  -e USERS_CSV=/scripts/users.csv `
  -e ADMIN_EMAIL=admin@test.com `
  -e ADMIN_PASSWORD=admin1111 `
  -e ENABLE_ACADEMY_CREATE=false `
  -e SEED_STUDENTS=0 `
  -e RUN_ID=multi50 `
  -e RATE=10 `
  -e DURATION=10m `
  -e RECEIPT_NEW_STUDENT=true `
  -e ENABLE_ATTENDANCE_WRITE=false `
  /scripts/attendance_receipt_mixed.js
```

## 6) 최신 테스트 결과 (multi50)
### k6 결과
- http_req_failed: 0.01% (1/6013)
- p95 / p99: 74.26ms / 282.64ms
- 평균 응답: 33.42ms
- 총 요청: 6013
- 5xx: 1건 (학생 전화번호 유니크 제약 충돌)

### Grafana 관찰
- Hikari Active: 0
- Hikari Pending: 0
- MySQL QPS: ~3.93
- Slow Queries: 0

## 7) 결론
- 50명 다중 유저/다중 학원 조회 부하에서 응답 안정적
- Redis 캐시 초기화 후 500 에러 해결
- 다음 단계: RPS 상향(20~30) 또는 쓰기 비중 증가 테스트

## 8) 다음 진행 계획
1. 안정성 먼저
   - Redis 캐시 역직렬화 오류 원인 확정
     - 캐시 키/직렬화 방식 점검(타입 정보 포함 여부)
     - 필요 시 캐시 초기화 자동화(테스트 전 `FLUSHALL` 또는 키 prefix 삭제)
   - 학생/수납 생성 시 중복 전화번호 방지 로직 확인
     - k6 스크립트에 “유저별 고유 전화번호 범위” 적용
     - 또는 테스트 중 쓰기 비중 낮추기

2. 부하 스케일 업
   - RPS 단계적 상승: 10 → 20 → 30 (각 10분)
   - 목표: p95 < 500ms 유지, 5xx = 0

3. 시나리오 분리 테스트
   - 조회-only 시나리오(캐시 히트율 확인)
   - 혼합 시나리오(조회:쓰기 = 8:2 또는 9:1)
   - 쓰기-heavy 시나리오(부하 시 안정성 확인)

4. 핵심 API 별 지표 수집
   - 느린 엔드포인트 Top3 추적
   - Prometheus 쿼리로 endpoint별 p95/p99 추적
   - 병목이 보이면 SQL/인덱스/캐시 정책 점검

5. 데이터 규모 확대
   - 학원 수 50 → 100
   - 학원당 학생 수 20 → 50
   - 데이터가 커질 때 조회 성능 변화 확인

6. 리소스 상관관계 분석
   - Hikari Active/Pending, MySQL QPS/Slow query, JVM Heap/CPU를 같이 보기
   - 병목이 보이는 순간의 로그/지표 스냅샷 기록
