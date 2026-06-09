# DB 적용 안내 (외부 Aiven MySQL)

DBeaver / MySQL Workbench에서 `defaultdb` 에 접속한 뒤 **아래 순서대로** 실행한다.
접속 정보는 프로젝트 루트 `.env` 참고 (host: `...aivencloud.com:26156`, SSL 필수).

## 실행 순서

1. **`schema.sql`** — 테이블 7개 생성 + `level_code` 5행 + 테스트 사용자 2명.
   - 앞부분 `DROP TABLE IF EXISTS ...` 는 산책 서비스 테이블만 지운다(기존 madang `Customer` 등은 안전).

2. **`function.sql`** — `fn_distance_km` 등록.
   - mysql CLI/Workbench: 파일 그대로 실행(`DELIMITER //` 포함).
   - **DBeaver**: `DROP FUNCTION ...` 한 줄을 먼저 실행 → `CREATE FUNCTION ... END` 본문을 `DELIMITER` 없이 블록 실행.

3. **`load_data.sql`** — CSV 적재 + 정규화.
   - 파일 안 `'<<<CSV_PATH>>>'` 를 CSV 절대경로로 교체. 예) `'C:/Users/dnxo1/Downloads/내_주변_산책로_데이터.csv'` (슬래시 `/` 사용)
   - `LOAD DATA LOCAL INFILE` 전제:
     - 서버: `SET GLOBAL local_infile = 1;` (Aiven 권한 제한 시 콘솔/지원 통해 활성화)
     - DBeaver: 연결 설정 → Driver properties 에 `allowLoadLocalInfile = true` 추가
   - 적재된 행수가 0이거나 좌표가 깨지면 `LINES TERMINATED BY` 를 `'\r\n'` ↔ `'\n'` 으로 바꿔 재시도.

## 적재 검증 (load_data.sql 마지막 SELECT)
- `course_cnt` ≈ 1623 (좌표 결측 행 제외되어 약간 적을 수 있음)
- `time_null` 비율 ≈ 5.8% (명세서대로 파싱 불가분은 NULL)
- `fn_distance_km(35.87,128.60,lat,lon)` 거리값 정상 반환

## 적재 후 서버 실행 & API 테스트
프로젝트 루트에서:
```
mvnw spring-boot:run        # .env 주입은 IntelliJ EnvFile 또는 환경변수로
```
스모크 테스트(예):
```
curl "http://localhost:8080/api/courses?sido=경기&maxLevel=2&maxLength=5&sort=length"
curl "http://localhost:8080/api/courses/nearby?lat=35.87&lon=128.60&limit=5"
curl "http://localhost:8080/api/courses/{course_id}"
curl -X POST "http://localhost:8080/api/courses/{course_id}/favorite?userId=1"
curl -X POST "http://localhost:8080/api/courses/{course_id}/reviews" -H "Content-Type: application/json" -d "{\"userId\":1,\"rating\":5,\"comment\":\"좋아요\"}"
curl -X POST "http://localhost:8080/api/walk-logs" -H "Content-Type: application/json" -d "{\"userId\":1,\"courseId\":\"{course_id}\",\"walkedOn\":\"2026-06-08\",\"spentMin\":40}"
curl "http://localhost:8080/api/users/me/stats?userId=1"
```
> 테스트 사용자 user_id 는 schema.sql 의 AUTO_INCREMENT 순서상 1(tester1), 2(tester2).
