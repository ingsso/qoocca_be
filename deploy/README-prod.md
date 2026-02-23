# Production Deploy (Artifact-based)

## 1) EC2 준비
- `/opt/qoocca/config` 디렉토리 생성
- `/opt/qoocca/logs` 디렉토리 생성
- 운영용 `application-docker.yml`을 `/opt/qoocca/config/application-docker.yml`로 배치

## 2) 환경변수 파일
- `.env.prod.example`를 복사해서 `.env.prod` 생성
- 실제 값으로 수정

## 3) 실행
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod pull
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
```

## 4) 확인
```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f api
curl -i http://localhost:8080/actuator/health
```

## 참고
- Git에는 `qoocca-api/src/main/resources/application*.yml`을 올리지 않고, EC2 외부 설정 파일로 관리합니다.
- RDS 보안그룹은 `3306`을 EC2 보안그룹에서만 접근 허용해야 합니다.
