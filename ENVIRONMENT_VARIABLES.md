# Environment Variable Keys

This project reads runtime values from `.env` (local) or server environment variables (prod).

## Required keys

- `MYSQL_ROOT_PASSWORD`
- `MYSQL_DATABASE`
- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_PORT`
- `SECRET_KEY`
- `ENCRYPTION_KEY`
- `cors.allowed-origins`
- `KAKAO_CLIENT_ID`
- `KAKAO_REDIRECT_URI`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`
- `NAVER_REDIRECT_URI`
- `OPENAI_API_KEY`

## Profile DB policy

- `local`: `spring.jpa.hibernate.ddl-auto=update`, `spring.sql.init.mode=always`
- `prod`: `spring.jpa.hibernate.ddl-auto=validate`, `spring.sql.init.mode=never`
