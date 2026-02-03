# qoocca-teachers 리팩토링 릴리즈 노트

- 배포일: 2026-02-03
- 대상 브랜치/커밋: refactor/1

## 변경 요약
- refresh 쿠키 보안 설정 외부화(local/prod 분리)
- DB 초기화 정책 profile 분리(local always / prod never)
- 전역 예외 응답 표준화 및 내부 메시지 노출 최소화
- 테스트 실행 환경(JUnit Platform) 복구 + 회귀 테스트 추가
- 결제 흐름 단일화(PaymentService -> ReceiptService 위임)
- 결제 상태전이 규칙 강화 + 권한/상태 테스트 추가
- 중복 DTO 제거(`api.user.model` 삭제, `common.auth.model` 단일화)
- OpenAI timeout/RestTemplate 설정, Firebase/FCM/SMS 로깅 표준화

## API 영향
- 유지(하위 호환): `/api/payment/complete` (deprecated)
- 권장(신규): `/api/receipt/{receiptId}/pay`

## 설정 영향
- 프로필 파일:
  - `qoocca-api/src/main/resources/application-local.yml`
  - `qoocca-api/src/main/resources/application-prod.yml`
- 환경변수 문서:
  - `ENVIRONMENT_VARIABLES.md`

## 검증 결과
- 실행 명령:
  - `.\gradlew :qoocca-common:test :qoocca-db:test :qoocca-auth:test :qoocca-api:test --console=plain`
  - `.\gradlew :qoocca-api:compileJava`
- 결과:
  - 테스트 성공
  - 컴파일 성공

## 남은 리스크 / 후속
- 일부 파일에 한글 인코딩 깨짐 문자열이 남아 있어 별도 정리 권장
- 레거시 엔드포인트 `/api/payment/complete` 제거 일정 확정 필요
