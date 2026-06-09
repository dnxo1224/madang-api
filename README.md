# 산책로 기록 서비스 (Walking Course API)

> **DB 프로그래밍 과제** — 산책 관련 공공데이터를 활용하는 Spring Boot REST API 서버

---

## 프로젝트 개요

전국 산책로 공공데이터(1,623건)를 기반으로 코스 검색, 즐겨찾기, 후기 작성, 산책 기록 통계 기능을 제공하는 백엔드 서비스.

### 기술 스택

| 구분 | 내용 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.5 |
| DB 접근 | Spring JdbcTemplate |
| DBMS | MySQL 8.0 (Aiven Cloud) |
| 빌드 | Maven |
| 환경변수 | `.env` 파일 (IntelliJ EnvFile 플러그인) |

---

## 프로젝트 구조

```
src/main/java/com/example/walking/
├── controller/
│   ├── CourseController.java   # /api/courses 엔드포인트
│   ├── WalkLogController.java  # /api/walk-logs, /api/users/me/stats
│   └── GlobalExceptionHandler.java
├── service/
│   ├── CourseService.java
│   ├── FavoriteService.java
│   ├── ReviewService.java
│   ├── WalkLogService.java
│   └── WalkStatsService.java
├── repository/
│   ├── CourseRepository.java   # fn_distance_km native query 포함
│   ├── FavoriteRepository.java
│   ├── ReviewRepository.java
│   └── WalkLogRepository.java
├── domain/                     # Course, Region, LevelCode, AppUser 등
├── dto/                        # 요청/응답 DTO
└── exception/                  # NotFoundException, BadRequestException 등

src/main/resources/
├── application.properties
└── db/
    ├── schema.sql              # 테이블 7개 DDL
    ├── function.sql            # fn_distance_km 함수
    └── load_data.sql           # CSV 적재 SQL (참고용)
```

---

## DB 설계

### 테이블 구성

| 테이블 | 역할 |
|--------|------|
| `course` | 원천 산책로 데이터 (1,623건) |
| `region` | 시·도 / 시·군·구 정규화 |
| `level_code` | 난이도 코드 (1=매우쉬움 ~ 5=매우어려움) |
| `app_user` | 서비스 가입 사용자 |
| `favorite` | 사용자–코스 즐겨찾기 (N:M) |
| `review` | 코스 후기·별점 (코스당 1인 1리뷰) |
| `walk_log` | 사용자 산책 실적 기록 |

### Stored Function

```sql
-- 하버사인 공식 기반 두 지점 간 거리 계산 (km)
fn_distance_km(lat1, lon1, lat2, lon2) RETURNS DECIMAL(8,3)
```

DB에 남기는 유일한 stored program. 전체 코스(1,623건)를 앱으로 끌어오지 않고 DB 안에서 거리 정렬·LIMIT 처리하기 위해 사용.

### 추가 테이블로 가능해진 기능

| 추가 요소 | 가능해진 서비스 |
|-----------|----------------|
| `region` 분리 | 시·도 단위 코스 검색·집계 |
| `level_code` | 난이도 범위 필터 |
| `length_km` / `time_min` 파생 | 거리·시간 범위 검색, 거리순 정렬 |
| `favorite` | 즐겨찾기 추가/해제 |
| `review` | 평균 별점 조회 |
| `walk_log` | 누적 거리/횟수 통계 |

---

## 실행 방법

### 1. 환경변수 설정

프로젝트 루트에 `.env` 파일 생성 (`.gitignore`에 포함됨):

```
DB_URL=jdbc:mysql://<host>:<port>/defaultdb?ssl-mode=REQUIRED
DB_USERNAME=avnadmin
DB_PASSWORD=<비밀번호>
```

### 2. 앱 실행

IntelliJ에서 EnvFile 플러그인으로 `.env` 로드 후 `DemoStep3Application` 실행.

### 3. CSV 데이터 적재 (최초 1회)

`application.properties`에 아래 두 줄 추가 후 앱 실행:

```properties
csv.loader.enabled=true
csv.loader.path=C:/경로/내_주변_산책로_데이터.csv
```

콘솔에 `[CsvDataLoader] 완료 — 적재: xxxx행` 메시지 확인 후 `false`로 되돌린다.

---

## API 목록

| Method | Path | 기능 |
|--------|------|------|
| GET | `/api/courses` | 조건검색 (시도·난이도·거리·정렬) |
| GET | `/api/courses/nearby` | 내 주변 코스 N개 |
| GET | `/api/courses/{id}` | 코스 상세 + 평균평점 |
| POST | `/api/courses/{id}/favorite` | 즐겨찾기 추가 |
| DELETE | `/api/courses/{id}/favorite` | 즐겨찾기 해제 |
| POST | `/api/courses/{id}/reviews` | 후기 작성 |
| POST | `/api/walk-logs` | 산책 기록 추가 |
| GET | `/api/users/me/stats` | 나의 산책 통계 |

### 요청 예시

```
# 경기도 쉬움 이하 코스 검색
GET /api/courses?sido=경기&maxLevel=2&maxLength=10&sort=length

# 내 주변 5개 (위도·경도 입력)
GET /api/courses/nearby?lat=35.87&lon=128.60&limit=5

# 코스 상세
GET /api/courses/KCCWSPO20N000000001

# 즐겨찾기 추가
POST /api/courses/KCCWSPO20N000000001/favorite?userId=1

# 리뷰 작성
POST /api/courses/KCCWSPO20N000000001/reviews
{"userId": 1, "rating": 5, "comment": "좋은 코스입니다"}

# 산책 기록
POST /api/walk-logs
{"userId": 1, "courseId": "KCCWSPO20N000000001", "walkedOn": "2026-06-08", "spentMin": 90}

# 나의 통계
GET /api/users/me/stats?userId=1
```

### 프로시저
- sp_toggle_favorite	즐겨찾기 — 없으면 추가, 있으면 삭제	IF/ELSE 분기
- sp_add_review	리뷰 작성 — 사용자/코스 존재 검증 + INSERT	유효성 검사 포함
- sp_user_walk_stats	나의 통계 — 횟수·누적거리·자주 간 코스 집계	복잡한 집계 쿼리

---

## 설계 원칙

> **로직은 애플리케이션(Spring Boot), 무결성은 DB**

- Stored Program은 `fn_distance_km` 함수 하나만 DB에 유지
- 통계·집계 등 나머지 비즈니스 로직은 Service 계층에서 처리
- 인증 미구현 → `userId`를 쿼리 파라미터로 전달하는 방식 사용

---

## 제한 사항

- 좌표가 코스의 대표 지점 1개만 제공되므로 정밀 경로 안내는 지원 불가
- `COURS_TIME_CN`(소요시간) 자유 텍스트 파싱 커버율 약 94% — 나머지는 `NULL`
- 별도 인증 없이 `userId` 파라미터로 사용자 식별 (과제 범위 외)
