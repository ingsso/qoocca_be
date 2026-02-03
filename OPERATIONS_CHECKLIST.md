# 운영 체크리스트

## 사전 점검
- [ ] 배포 브랜치/태그 확인
- [ ] `.env` 또는 서버 환경변수 적용 완료
- [ ] DB 백업 완료
- [ ] Redis/DB 연결 가능 확인

## 프로필/설정 점검
- [ ] `local`: `ddl-auto=update`, `sql.init.mode=always`
- [ ] `prod`: `ddl-auto=validate`, `sql.init.mode=never`
- [ ] OpenAI timeout 설정 확인(`connect=2000ms`, `read=5000ms`)
- [ ] refresh cookie 정책(local/prod) 확인

## 애플리케이션 점검
- [ ] 앱 기동 성공
- [ ] 치명적 오류 로그 없음
- [ ] Firebase 초기화 실패 시에도 서비스 기동 유지 확인

## 기능 스모크 테스트
- [ ] 로그인 / refresh / logout
- [ ] `POST /api/receipt/{receiptId}/pay`
- [ ] `POST /api/receipt/{receiptId}/cancel`
- [ ] `POST /api/payment/complete` (하위 호환)
- [ ] 에러 응답 포맷(`status/code/message`) 확인

## 배포 후 모니터링 (30~60분)
- [ ] 5xx 비율 급증 여부
- [ ] 인증 실패율 이상 여부
- [ ] 결제 상태전이 오류(`INVALID_RECEIPT_STATUS`) 증가 여부
- [ ] OpenAI/FCM 경고 로그 과다 여부
