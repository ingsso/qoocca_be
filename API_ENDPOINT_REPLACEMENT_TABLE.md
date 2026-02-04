# FE API 치환표 (레거시 -> 신규)

> 원칙: 백엔드는 레거시/신규 경로를 동시 지원 중이므로, FE를 신규 경로로 순차 전환하면 됩니다.

| 영역 | FE 파일 | 메서드 | 레거시 경로 | 신규 경로 |
|---|---|---|---|---|
| 학원 등록 | `src/api/academyApi.ts` | POST | `/api/academy/register` | `/api/academy/registrations` |
| 내 학원 목록 | `src/api/academyApi.ts`, `src/api/dashboardApi.ts` | GET | `/api/academy/academy-list` | `/api/me/academies` |
| 내 등록 상태 | `src/api/academyApi.ts` (사용 시) | GET | `/api/academy/check-registration` | `/api/me/academy-registration` |
| 학원 상세 | `src/api/dashboardApi.ts`, `modify/page.tsx` | GET | `/api/academy/{academyId}` | `/api/academy/{academyId}/profile` |
| 학원 수정 | `modify/page.tsx` | PUT | `/api/academy/{academyId}` | `PATCH /api/academy/{academyId}/profile` |
| 재심사 요청 | `src/api/dashboardApi.ts` | PUT | `/api/academy/{academyId}/resubmit` | `POST /api/academy/{academyId}/approval/resubmissions` |
| 과목 조회 | `class/register/page.tsx` | GET | `/api/academy/{academyId}/subjects` | `/api/academy/{academyId}/curriculum/subjects` |
| 연령대 조회 | `class/register/page.tsx` | GET | `/api/academy/{academyId}/ages` | `/api/academy/{academyId}/curriculum/ages` |
| 대시보드 통계 | `src/api/dashboardApi.ts`, `src/api/paymentApi.ts` | GET | `/api/academy/{academyId}/stats` | `/api/academy/{academyId}/dashboard/stats` |
| 반 요약 | `src/api/dashboardApi.ts` | GET | `/api/academy/{academyId}/class/summary` | `/api/academy/{academyId}/dashboard/class-summary` |
| 대시보드 수납 요약 | `src/api/dashboardApi.ts` | GET | `/api/academy/{academyId}/receipt/dashboard-main` | `/api/academy/{academyId}/dashboard/receipt-main` |
| 반별 수납 요약 | `src/api/paymentApi.ts` | GET | `/api/academy/{academyId}/receipt/class-summary` | `/api/academy/{academyId}/dashboard/receipt-class-summary` |
| 반 통계 | `src/api/statsApi.ts` | GET | `/api/academy/{academyId}/class/stats` | `/api/academy/{academyId}/analytics/class-stats` |
| 보호자 통계 | `src/api/parentstatsApi.ts` | GET | `/api/academy/{academyId}/class/parentstats` | `/api/academy/{academyId}/analytics/parent-stats` |

## FE 적용 체크리스트

1. `src/api/*Api.ts`에 엔드포인트 상수(`ACADEMY_ENDPOINTS`)를 추가해 하드코딩을 제거합니다.
2. 경로가 바뀐 API는 HTTP Method도 같이 반영합니다.
   - 학원 수정: `PUT` -> `PATCH`
   - 재심사 요청: `PUT` -> `POST`
3. 페이지(`modify/page.tsx`, `class/register/page.tsx`)에서 직접 호출 중이면 API 모듈 함수 호출로 통일합니다.
4. 전환 후 네트워크 탭에서 신규 경로 호출 여부를 확인합니다.

## 참고

- 백엔드는 현재 레거시/신규 경로를 모두 허용합니다(점진 전환 목적).
- FE 전환 완료 후 호출 로그를 보고 레거시 경로 제거 여부를 결정하면 됩니다.