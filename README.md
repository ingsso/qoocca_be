# qoocca-teachers

학원(academy) 운영에 필요한 **학생/부모/출결/수납/클래스 관리**를 제공하는 백엔드 서비스입니다.
Spring Boot 멀티 모듈 구조로 **보안/데이터/공통 모듈을 분리**해 유지보수성과 확장성을 높였고,
**Redis 캐시, Prometheus/Grafana 모니터링, k6 성능 테스트**까지 포함한 운영 친화적 구성을 갖추고 있습니다.

---

## 핵심 특징
- **멀티 모듈 구조**로 도메인/보안/공통 관심사를 명확히 분리
- **JWT + OAuth2(Kakao/Naver)** 기반 인증/인가
- **Redis 캐시**로 조회 성능 강화
- **Flyway** 기반 DB 마이그레이션
- **Prometheus + Grafana**로 운영 지표 시각화
- **k6 성능 테스트** 스크립트 포함
- **Swagger(OpenAPI)** 문서 제공
- **Nginx 이미지 서버** 연동

---

## 모듈 구성
| 모듈 | 역할 |
| --- | --- |
| `qoocca-api` | API 컨트롤러/서비스 계층. 학원, 클래스, 학생, 출결, 수납, 부모 인증 등 핵심 비즈니스 로직 |
| `qoocca-auth` | Spring Security, JWT, OAuth2, 승인 필터 등 인증/인가 |
| `qoocca-db` | JPA 엔티티/리포지토리, 데이터 시드, DB 접근 계층 |
| `qoocca-common` | 공통 유틸/예외/DTO/Redis DAO |

---

## 기술 스택
- **Java / Spring Boot 3.3.5**
- Spring Security, Spring Data JPA, Spring Data Redis
- MySQL 8.x, Redis
- Flyway
- Prometheus / Grafana
- k6 (부하 테스트)
- Nginx (이미지 서빙)
- Swagger/OpenAPI (`springdoc-openapi`)

---

## 아키텍처 개요
```
Client (Web/App)
  |
  v
qoocca-api (Spring Boot)
  |-- qoocca-auth   (Security/JWT/OAuth2)
  |-- qoocca-db     (JPA/Repository/Flyway)
  `-- qoocca-common (Utils/DTO/Error/Redis DAO)

External Services (Docker Compose)
  - MySQL
  - Redis
  - Nginx (image server)
  - Prometheus / Grafana
  - k6 (load test)
```

---

## API 엔드포인트
아래 표는 컨트롤러 기준의 **주요 엔드포인트 요약**입니다. 자세한 스펙은 Swagger를 참고하세요.

### 인증/사용자
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/auth/signup` | 회원가입 |
| `POST` | `/api/auth/link-social` | 소셜 계정 연동 |
| `POST` | `/api/auth/login` | 이메일/비밀번호 로그인 |
| `POST` | `/api/auth/{provider}` | 소셜 로그인 (kakao/naver) |
| `POST` | `/api/auth/refresh` | Access Token 재발급 |
| `POST` | `/api/auth/logout` | 로그아웃 |
| `POST` | `/api/auth/send-code` | SMS 인증코드 발송 |
| `POST` | `/api/auth/verify-code` | SMS 인증코드 검증 |

### 부모 인증
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/parent/auth/login` | 부모 로그인 |

### 학원/프로필/대시보드
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/academy/registrations` | 학원 등록 (multipart) |
| `GET` | `/api/academy/{id}/profile` | 학원 상세 프로필 조회 |
| `PATCH` | `/api/academy/{id}/profile` | 학원 프로필 수정 (multipart) |
| `GET` | `/api/academy/{id}/curriculum/subjects` | 학원 과목 목록 |
| `GET` | `/api/academy/{id}/curriculum/ages` | 학원 연령대 목록 |
| `POST` | `/api/academy/{id}/images` | 학원 이미지 업로드 (multipart) |
| `GET` | `/api/academy/{id}/images/uploads/{jobId}` | 이미지 업로드 상태 조회 |
| `DELETE` | `/api/academy/{id}/images/{imageId}` | 학원 이미지 삭제 |
| `POST` | `/api/academy/{id}/files` | 학원 서류/이미지 업로드 (multipart) |
| `POST` | `/api/academy/{id}/approval/resubmissions` | 승인 재신청 (multipart) |
| `GET` | `/api/academy/{id}/dashboard/stats` | 학원 대시보드 통계 |
| `GET` | `/api/me/academy-registration` | 내 학원 등록 상태 |
| `GET` | `/api/me/academies` | 내 학원 목록 |

### 학원 학생
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/academy/{academyId}/student` | 학원 학생 등록 |
| `POST` | `/api/academy/{academyId}/student/with-parent` | 학생+부모 동시 등록 |
| `PUT` | `/api/academy/{academyId}/student/{studentId}` | 학생 정보 수정 |
| `GET` | `/api/academy/{academyId}/student` | 학원 학생 목록 |
| `DELETE` | `/api/academy/{academyId}/student/{studentId}` | 학생 삭제 |
| `POST` | `/api/academy/{academyId}/student/upload` | 엑셀 업로드 등록 (multipart) |

### 클래스/반 관리
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/academy/{academyId}/class` | 클래스 생성 |
| `GET` | `/api/academy/{academyId}/class` | 클래스 목록 |
| `PUT` | `/api/academy/{academyId}/class/{classId}/student/{studentId}/move` | 학생 반 이동 |

### 클래스 학생
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/academy/{academyId}/class/{classId}/student` | 기존 학생 반 등록 |
| `PUT` | `/api/academy/{academyId}/class/{classId}/student/{studentId}` | 반 내 상태 수정 |
| `GET` | `/api/academy/{academyId}/class/{classId}/student` | 반 학생 목록 |
| `DELETE` | `/api/academy/{academyId}/class/{classId}/student/{studentId}` | 반 학생 제거 |

### 출결
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/student/{studentId}/attendance` | 출결 체크인 |
| `GET` | `/api/student/{studentId}/attendance` | 출결 조회 (date 필요) |
| `PATCH` | `/api/student/{studentId}/attendance/check-out` | 출결 체크아웃 |
| `GET` | `/api/attendance/{studentId}/calendar-view` | 출결 캘린더 (academyId/year/month) |
| `GET` | `/api/attendance/academy/{academyId}/today` | 학원별 오늘 출결 목록 |
| `GET` | `/api/attendance/academy/{academyId}/summary` | 학원별 출결 요약 |
| `GET` | `/api/attendance/class/{classId}/monthly-stats` | 반 월간 통계 |

### 수납
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/student/{studentId}/receipt` | 수납 생성 |
| `GET` | `/api/student/{studentId}/receipt` | 학생 수납 목록 |
| `GET` | `/api/student/{studentId}/receipt/month` | 월별 수납 조회 |
| `PUT` | `/api/student/{studentId}/receipt/{receiptId}` | 수납 상태 수정 |
| `POST` | `/api/receipt/{receiptId}/pay` | 결제 처리 (부모) |
| `POST` | `/api/receipt/{receiptId}/cancel` | 결제 취소 (부모) |
| `GET` | `/api/parent/receipt/requests` | 부모 결제요청 목록 |
| `GET` | `/api/parent/receipt/{receiptId}` | 부모 결제요청 상세 |
| `GET` | `/api/academy/{academyId}/dashboard/receipt-class-summary` | 반별 수납 요약 |
| `GET` | `/api/academy/{academyId}/dashboard/receipt-main` | 대시보드 수납 요약 |

### 학원 분석/대시보드
| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/academy/{academyId}/dashboard/class-summary` | 대시보드 클래스 요약 |
| `GET` | `/api/academy/{academyId}/analytics/class-stats` | 클래스 통계 |
| `GET` | `/api/academy/{academyId}/analytics/parent-stats` | 부모 통계 |

### 관리자
| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/admin/academy/{id}/approve` | 학원 승인 |
| `POST` | `/api/admin/academy/{id}/reject` | 학원 반려 |
| `GET` | `/api/admin/academy/pending` | 승인 대기 목록 |
| `GET` | `/api/admin/academy/rejected` | 반려 목록 |
| `GET` | `/api/admin/academy` | 전체 학원 목록 |
| `GET` | `/api/admin/academy/{id}` | 학원 상세 (관리자) |

### 공통/메타
| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/ages` | 연령대 목록 |
| `GET` | `/api/subjects` | 과목 목록 |
| `POST` | `/api/fcm/register` | FCM 토큰 등록 |

---

## 인증 플로우
```
사용자 로그인
  1) /api/auth/login (또는 /api/auth/{provider})
  2) Access Token 응답 + Refresh Token 쿠키 발급
  3) 이후 요청: Authorization 헤더에 Access Token 포함
  4) 만료 시 /api/auth/refresh 로 재발급

부모 로그인
  1) /api/parent/auth/login
  2) 부모 전용 권한으로 수납 결제/조회
```

---

## 권한/인가 정책 요약
SecurityConfig 및 AcademyApprovalFilter 기준의 요약입니다.

### Public (인증 불필요)
| Path | 비고 |
| --- | --- |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | Swagger |
| `/actuator/**` | Actuator |
| `/api/auth/**` | 회원가입/로그인/토큰 |
| `/api/parent/auth/login` | 부모 로그인 |
| `/oauth2/**` | OAuth2 콜백 |
| `/api/ages`, `/api/subjects` | 공통 조회 |
| `/api/fcm/register` | FCM 토큰 등록 |

### Role 기반
| Role | Path |
| --- | --- |
| `ADMIN` | `/api/admin/**` |
| `ADMIN` | `DELETE /api/class/**` |
| `PARENT` | `/api/parent/**` |
| `PARENT` | `POST /api/receipt/*/pay`, `POST /api/receipt/*/cancel` |
| `USER` or `ADMIN` | `/api/academy/**`, `/api/class/**`, `/api/student/**`, `/api/attendance/**`, `/api/me/**` |

### 학원 승인(Approval) 필터
인증된 사용자에 대해 **학원 승인 여부**를 추가 검증합니다.
| 제외 경로(승인 체크 생략) |
| --- |
| `/api/auth/**` |
| `/api/admin/**` |
| `/api/ages/**`, `/api/subjects/**` |
| `/api/academy/registrations` |
| `/api/academy/*/files` |
| `/api/academy/*/approval/resubmissions` |
| `/api/me/**` |
| `/api/attendance/**` |
| `/api/academy/*/class/*/student/*/move` |
| `/swagger-ui/**`, `/v3/api-docs/**` |

추가로 `GET /api/academy` 는 승인 여부와 무관하게 접근 가능하며,  
`GET /api/academy/{id}` / `GET /api/academy/{id}/profile` 는 **본인이 소유한 학원**인 경우 허용됩니다.

---

## 요청/응답 예시
### 1) 로그인
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@test.com",
    "password": "password123"
  }'
```

### 2) 학원 등록 (multipart)
```bash
curl -X POST http://localhost:8080/api/academy/registrations \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -F "name=Qoocca Academy" \
  -F "address=Seoul" \
  -F "businessNumber=123-45-67890" \
  -F "certificateFile=@/path/to/cert.pdf"
```

### 3) 출결 체크인
```bash
curl -X POST http://localhost:8080/api/student/1/attendance \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "checkInTime": "09:00"
  }'
```

---

## 빠른 시작 (로컬)
1. `.env.local.example` → `.env` 또는 `.env.local` 복사
2. `SPRING_PROFILES_ACTIVE=local` 설정
3. 실행
```powershell
./gradlew :qoocca-api:bootRun
```

로컬 프로파일 특징:
- 캐시 비활성화 (`spring.cache.type=none`)
- SQL init 활성화 (`spring.sql.init.mode=always`)
- `ddl-auto=update`

---

## Docker 실행
1. `.env.docker.example` → `.env.docker` 복사
2. 실행
```powershell
docker compose up -d --build api
```

Docker 프로파일 특징:
- Redis/MySQL 컨테이너 사용
- 캐시 활성화
- HikariCP 풀 사이즈 증가
- 이미지 업로드 경로 `/app/uploads/`

---

## 배포 (EC2, JAR 업로드)
OS: **Ubuntu 24.04 (noble)**

### 개요
로컬에서 빌드한 JAR를 EC2에 업로드해 실행하는 방식입니다.  
HTTPS(SSL) 적용을 위해 Nginx 리버스 프록시를 사용합니다.

### 빌드
```powershell
./gradlew :qoocca-api:bootJar
```

### 업로드
```bash
scp build/libs/*.jar ubuntu@<EC2_HOST>:/opt/qoocca/qoocca-api.jar
```

### 실행 (예시)
```bash
java -jar /opt/qoocca/qoocca-api.jar --spring.profiles.active=prod
```

### 설정 파일 참고 경로
프로젝트 내 설정 파일은 아래 위치에 있습니다. EC2 환경에서는 동일한 내용을 서버 경로로 옮겨 사용하세요.
- `D:\qoocca-teachers\qoocca-api\src\main\resources\application-prod.yml`
- `D:\qoocca-teachers\.env.prod.example`
- `D:\qoocca-teachers\docker-compose.prod.yml`

### 포트/HTTPS
- 애플리케이션 포트: `8080`
- HTTPS 적용: **SSL 사용 (Nginx 리버스 프록시)**

---

## 환경 변수
주요 환경 변수 (예시):
- `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`, `MYSQL_PORT`
- `SECRET_KEY`, `ENCRYPTION_KEY` (JWT/쿠키 암호화)
- `KAKAO_CLIENT_ID`, `KAKAO_REDIRECT_URI`
- `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`, `NAVER_REDIRECT_URI`
- `OPENAI_API_KEY`

자세한 설정은 `docs/ENV_SETUP.md` 참고.

---

## API 문서
Swagger UI:
- `/swagger-ui/index.html`
- `/v3/api-docs`

---

## 모니터링
- **Prometheus**: `http://localhost:9090`
- **Grafana**: `http://localhost:3100` (기본 계정 admin/admin)
- **MySQL Exporter**: `http://localhost:9104/metrics`

---

## 성능 테스트 (k6)
기본 스크립트:
- `performance/k6/attendance_receipt_mixed.js`
- `performance/k6/seed_multi_academy.js`

실행 예시:
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

성능 결과 요약은 `docs/perf-test-summary-2026-02-09.md` 참고.

---

## 디렉터리 구조
```
.
├─ qoocca-api
├─ qoocca-auth
├─ qoocca-common
├─ qoocca-db
├─ monitoring
├─ performance
├─ nginx
└─ docs
```

---

## 보안/인가 정책
엔드포인트 권한 정책 초안은 `docs/security-policy.md`에 정리되어 있습니다.
정확한 정책은 `qoocca-auth` 모듈의 Security 설정을 기준으로 합니다.

---

## 테스트
```powershell
./gradlew test
```

---

## 라이선스
프로젝트 정책에 따라 추가하세요.
