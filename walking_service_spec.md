# 산책로 공공데이터 기반 서비스 — 프로젝트 명세서

> **목적**: 이 문서는 Claude Code에서 프로젝트를 구현할 때 참조하는 전체 명세서입니다.
> 모든 설계 결정, DB 스키마, API 설계, 코드 구조가 이 문서에 담겨 있습니다.

---

## 1. 프로젝트 개요

### 1.1 과제 정의

- **과목**: 대학교 DB 프로그래밍
- **과제 내용**: 산책 관련 공공 데이터를 활용하는 서버를 구상하고 실제 동작하는 서버를 완성
- **핵심 요구사항**:
  - Stored program(어떤 형태로든) 하나 이상 포함
  - 추가 테이블과 그로 인해 가능해지는 서비스/쿼리를 설명하고 구현
  - 제한 사항 명시
  - 복잡하지 않아도 됨

### 1.2 기술 스택

| 구분 | 선택 |
|------|------|
| DBMS | MySQL 8.0 (Aiven Cloud) |
| Backend | Spring Boot 3.x |
| ORM | Spring Data JPA |
| 빌드 | Gradle |
| 언어 | Java 21 |
| DB 마이그레이션 | 직접 SQL 실행 (Flyway 미사용) |
| 환경변수 | `.env` 파일 (IntelliJ EnvFile 플러그인) |

### 1.3 설계 원칙

> **로직은 애플리케이션(Spring Boot), 무결성은 DB**

| 계층 | 담당 | 근거 |
|------|------|------|
| Spring Boot (Service/Repository) | 비즈니스 로직, 검증, 트랜잭션, 집계 | Git 버전관리·단위테스트·디버깅 용이 |
| DB (stored program) | 데이터 밀착 연산, 경로 무관 무결성 보장 | 대량 행을 앱으로 끌지 않고 DB 안에서 처리 |

- Stored program은 **거리 계산 함수(`fn_distance_km`) 하나만** DB에 둔다
- 통계 등 나머지 로직은 Spring Service 계층에서 처리한다

---

## 2. 원천 데이터 분석

### 2.1 CSV 파일 정보

- **파일명**: `내_주변_산책로_데이터.csv`
- **인코딩**: UTF-8 BOM
- **총 행수**: 1,623건
- **컬럼 수**: 16개

### 2.2 컬럼 목록

| 인덱스 | 컬럼명 | 설명 | 비고 |
|--------|--------|------|------|
| 0 | ESNTL_ID | 고유 ID | PK로 사용 (예: `KCCWSPO20N000000001`) |
| 1 | WLK_COURS_FLAG_NM | 상위 길 이름 | 해파랑길, 남산 둘레길 등 |
| 2 | WLK_COURS_NM | 코스 이름 | |
| 3 | COURS_DC | 코스 설명 | TEXT, 긴 서술형 |
| 4 | SIGNGU_NM | 시군구명 | '충남 청양군' 또는 '경기 고양시 덕양구' |
| 5 | COURS_LEVEL_NM | 난이도 | 5개 값: 매우쉬움, 쉬움, 보통, 어려움, 매우어려움 |
| 6 | COURS_LT_CN | 거리 구간 | 텍스트 범위: '5~10Km미만' 등 (사용 안 함) |
| 7 | COURS_DETAIL_LT_CN | 상세 거리 | 숫자 문자열: '13.8' (일부 파싱 불가) |
| 8 | ADIT_DC | 추가 설명 | TEXT |
| 9 | COURS_TIME_CN | 소요시간 | '2시간', '1시간 30분' 등 자유 텍스트 |
| 10 | OPTN_DC | 옵션 설명 | 265건 결측 |
| 11 | TOILET_DC | 화장실 정보 | 151건 결측 |
| 12 | CVNTL_NM | 편의시설 정보 | 253건 결측 |
| 13 | LNM_ADDR | 주소 | |
| 14 | COURS_SPOT_LA | 위도 | DECIMAL(10,7) |
| 15 | COURS_SPOT_LO | 경도 | DECIMAL(10,7) |

### 2.3 데이터 품질 이슈 및 정제 전략

| 이슈 | 처리 방법 |
|------|-----------|
| 난이도가 자유 텍스트 | `level_code` 코드 테이블로 분리 (1~5) |
| 거리가 구간 문자열 | `COURS_DETAIL_LT_CN`에서 실수값 추출 → `length_km` |
| 소요시간 자유 텍스트 | 패턴 매칭 4단계 UPDATE → `time_min` (94.2% 커버, 나머지 NULL) |
| SIGNGU_NM 합쳐짐 | 공백 분리해 `region` 테이블로 정규화 (2단어/3단어 패턴) |
| 좌표 1개만 제공 | 대표 지점 기준 근사 검색, 정밀 경로 안내는 범위 밖 |
| 결측값 다수 | toilet_info, store_info, description 등은 NULL 허용 |

#### SIGNGU_NM 분리 패턴

- **2단어** (1,528건): `'충남 청양군'` → sido=`충남`, sigungu=`청양군`
- **3단어** (95건): `'경기 고양시 덕양구'` → sido=`경기`, sigungu=`고양시 덕양구`

#### COURS_TIME_CN 파싱 패턴 (순차 적용)

1. `'N시간 M분'` / `'N시간M분'` → N×60 + M (앞에 '약' 있어도 처리)
2. `'N시간'` → N×60
3. `'N분'` → N
4. 순수 숫자 → 분으로 간주
5. 그 외 (`'4박 5일'`, `'왕복 3시간'`, `'3~4시간'` 등) → NULL

---

## 3. 데이터베이스 설계

### 3.1 테이블 구성

| 구분 | 테이블 | 역할 |
|------|--------|------|
| 기존(정제) | `course` | 원천 산책로. 거리/시간 수치 파생, 코드 FK 정규화 |
| 신규(코드) | `region` | 시·도 / 시·군·구 분리 |
| 신규(코드) | `level_code` | 난이도 코드(1~5)와 명칭 |
| 신규(기능) | `app_user` | 서비스 가입 사용자 |
| 신규(기능) | `favorite` | 사용자–코스 즐겨찾기 (N:M) |
| 신규(기능) | `review` | 코스 후기·별점 |
| 신규(기능) | `walk_log` | 사용자 산책 실적 기록 |

### 3.2 DDL — 코드 테이블

```sql
CREATE TABLE level_code (
    level_id   TINYINT     PRIMARY KEY,   -- 1=매우쉬움 … 5=매우어려움
    level_name VARCHAR(10) NOT NULL UNIQUE
);

INSERT INTO level_code VALUES
    (1, '매우쉬움'), (2, '쉬움'), (3, '보통'), (4, '어려움'), (5, '매우어려움');

CREATE TABLE region (
    region_id INT AUTO_INCREMENT PRIMARY KEY,
    sido      VARCHAR(20) NOT NULL,
    sigungu   VARCHAR(40) NOT NULL,
    UNIQUE (sido, sigungu)
);
```

### 3.3 DDL — course

```sql
CREATE TABLE course (
    course_id   VARCHAR(20)   PRIMARY KEY,    -- 원천 ESNTL_ID
    course_name VARCHAR(100)  NOT NULL,
    flag_name   VARCHAR(100),                 -- 상위 길(해파랑길 등)
    region_id   INT           NOT NULL,
    level_id    TINYINT       NOT NULL,
    length_km   DECIMAL(5,1),                 -- 텍스트 → 수치 파생
    time_min    INT,                          -- '1시간 30분' → 90 파생
    toilet_info VARCHAR(200),                 -- 결측 허용
    store_info  VARCHAR(200),                 -- 결측 허용
    lat         DECIMAL(10,7) NOT NULL,
    lon         DECIMAL(10,7) NOT NULL,
    description TEXT,
    FOREIGN KEY (region_id) REFERENCES region(region_id),
    FOREIGN KEY (level_id)  REFERENCES level_code(level_id),
    CHECK (length_km IS NULL OR length_km >= 0),
    CHECK (time_min  IS NULL OR time_min  >= 0)
);
```

### 3.4 DDL — 사용자 기능 테이블

```sql
CREATE TABLE app_user (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    login_id   VARCHAR(30) NOT NULL UNIQUE,
    nickname   VARCHAR(30) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE favorite (
    user_id   INT         NOT NULL,
    course_id VARCHAR(20) NOT NULL,
    added_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, course_id),
    FOREIGN KEY (user_id)   REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

CREATE TABLE review (
    review_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT         NOT NULL,
    course_id  VARCHAR(20) NOT NULL,
    rating     TINYINT     NOT NULL,
    comment    VARCHAR(500),
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (rating BETWEEN 1 AND 5),
    UNIQUE (user_id, course_id),    -- 코스당 1인 1리뷰
    FOREIGN KEY (user_id)   REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

CREATE TABLE walk_log (
    log_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT         NOT NULL,
    course_id  VARCHAR(20) NOT NULL,
    walked_on  DATE        NOT NULL,
    spent_min  INT,
    FOREIGN KEY (user_id)   REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id)
);
```

### 3.5 Stored Function — fn_distance_km

DB에 남기는 유일한 stored program. 하버사인 거리 계산을 캡슐화.
1,623건을 앱으로 끌어오지 않고 DB에서 정렬·LIMIT하기 위함.

```sql
DELIMITER //
CREATE FUNCTION fn_distance_km(
    lat1 DECIMAL(10,7), lon1 DECIMAL(10,7),
    lat2 DECIMAL(10,7), lon2 DECIMAL(10,7)
)
RETURNS DECIMAL(8,3)
DETERMINISTIC
BEGIN
    DECLARE R DOUBLE DEFAULT 6371;
    RETURN R * 2 * ASIN(SQRT(
        POWER(SIN(RADIANS(lat2 - lat1) / 2), 2) +
        COS(RADIANS(lat1)) * COS(RADIANS(lat2)) *
        POWER(SIN(RADIANS(lon2 - lon1) / 2), 2)
    ));
END //
DELIMITER ;
```

### 3.6 추가 테이블로 가능해지는 서비스

| 추가 요소 | 가능해지는 서비스 / 질의 |
|-----------|------------------------|
| `region` 분리 | 시·도 단위 코스 집계, 지역별 검색 |
| `level_code` | 난이도 정렬·범위필터 (level_id <= 2) |
| `length_km` / `time_min` 파생 | 거리·시간 범위검색, 거리순 정렬, 지역별 평균 |
| `favorite` | 내 즐겨찾기 목록, 가장 많이 찜한 코스 랭킹 |
| `review` | 평균 별점, 평점순 추천 |
| `walk_log` | 나의 누적 거리/횟수 통계 |

### 3.7 제약 조건 정리

| 제약 | 대상 | 설명 |
|------|------|------|
| PK/FK + CASCADE | favorite, review, walk_log | 사용자/코스 삭제 시 동반 정리 |
| CHECK | review.rating | 1~5 범위 |
| CHECK | course.length_km, time_min | 음수 불가 |
| UNIQUE | review(user_id, course_id) | 코스당 1인 1리뷰 |
| UNIQUE | app_user.login_id | 로그인 ID 중복 불가 |
| NOT NULL | 좌표, 이름, 지역, 난이도 | 필수값 |
| NULL 허용 | toilet_info, store_info, description, length_km, time_min | 결측 대비 |

---

## 4. CSV 데이터 적재 (MySQL 단에서 처리)

> **별도 SQL 파일** (`load_data.sql`)로 이미 작성 완료.
> 이 섹션은 그 스크립트의 흐름을 요약한 것.

### 적재 흐름

```
STEP 1  raw_course 임시 테이블 생성 (CSV 컬럼 그대로)
STEP 2  LOAD DATA LOCAL INFILE로 CSV → raw_course
STEP 3  region 테이블 채우기 (SIGNGU_NM 공백 분리, INSERT IGNORE)
STEP 4  course 테이블 채우기 (region·level_code 조인, length_km 파싱)
STEP 5  time_min UPDATE — 4패턴 순차 적용 (94.2% 커버)
STEP 6  fn_distance_km 함수 등록
STEP 7  raw_course DROP
STEP 8  적재 결과 확인 쿼리
```

### 주의사항

- `LOAD DATA LOCAL INFILE` 사용 전: `SET GLOBAL local_infile = 1;`
- CSV 경로는 본인 환경에 맞게 수정

---

## 5. REST API 설계

### 5.1 엔드포인트 목록

| Method | Path | 기능 | 처리 계층 |
|--------|------|------|-----------|
| GET | `/api/courses` | 조건검색 (지역·난이도·거리·정렬) | Repository 동적 쿼리 |
| GET | `/api/courses/nearby` | 내 주변 코스 N개 | Repository → DB 거리 함수 |
| GET | `/api/courses/{id}` | 코스 상세 + 평균평점 | Service 조합 |
| POST | `/api/courses/{id}/favorite` | 즐겨찾기 추가 | Service 트랜잭션 |
| DELETE | `/api/courses/{id}/favorite` | 즐겨찾기 해제 | Service 트랜잭션 |
| POST | `/api/courses/{id}/reviews` | 후기 작성 (별점) | Service + UNIQUE 제약 |
| POST | `/api/walk-logs` | 산책 기록 추가 | Service |
| GET | `/api/users/me/stats` | 나의 산책 통계 | Service 집계 (앱 계층) |

### 5.2 데이터 흐름 예시 — '내 주변 코스'

1. **Controller**: `GET /api/courses/nearby?lat=35.87&lon=128.60` 수신
2. **Service**: 좌표 유효성 검증
3. **Repository**: native query로 `fn_distance_km` 호출, DB에서 거리순 정렬 + LIMIT
4. **Service**: DTO 변환
5. **Controller**: JSON 응답

### 5.3 Repository에서 DB 함수 호출 예시

```java
public interface CourseRepository extends JpaRepository<Course, String> {

    @Query(value = """
        SELECT c.course_id, c.course_name,
               fn_distance_km(:lat, :lon, c.lat, c.lon) AS dist
        FROM course c
        ORDER BY dist ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<NearbyCourseView> findNearby(@Param("lat") double lat,
                                      @Param("lon") double lon,
                                      @Param("limit") int limit);
}
```

### 5.4 Service 계층 예시 — 나의 산책 통계

이전 구상의 `sp_user_walk_stats` 프로시저를 Service로 이동.

```java
@Service
@RequiredArgsConstructor
public class WalkStatsService {
    private final WalkLogRepository walkLogRepo;

    public WalkStatsDto getMyStats(int userId) {
        var summary = walkLogRepo.findSummary(userId);    // 횟수/누적거리/시간
        var top3    = walkLogRepo.findTopCourses(userId, 3); // 자주 간 코스
        return new WalkStatsDto(summary, top3);
    }
}
```

---

## 6. Spring Boot 프로젝트 구조

### 6.1 패키지 구조

```
src/main/java/com/example/walking/
├── WalkingApplication.java
├── entity/
│   ├── Course.java
│   ├── Region.java
│   ├── LevelCode.java
│   ├── AppUser.java
│   ├── Favorite.java
│   ├── FavoriteId.java          -- 복합키 클래스
│   ├── Review.java
│   └── WalkLog.java
├── repository/
│   ├── CourseRepository.java
│   ├── RegionRepository.java
│   ├── AppUserRepository.java
│   ├── FavoriteRepository.java
│   ├── ReviewRepository.java
│   └── WalkLogRepository.java
├── service/
│   ├── CourseService.java
│   ├── FavoriteService.java
│   ├── ReviewService.java
│   └── WalkStatsService.java
├── controller/
│   ├── CourseController.java
│   ├── FavoriteController.java
│   ├── ReviewController.java
│   └── WalkLogController.java
└── dto/
    ├── CourseSearchDto.java      -- 검색 조건
    ├── CourseDetailDto.java      -- 상세 응답
    ├── NearbyCourseDto.java      -- 주변 코스 응답
    ├── ReviewRequestDto.java     -- 리뷰 작성 요청
    ├── WalkLogRequestDto.java    -- 산책 기록 요청
    └── WalkStatsDto.java         -- 통계 응답
```

### 6.2 환경 설정

#### `.env` (프로젝트 루트, .gitignore에 포함)

```
DB_URL=jdbc:mysql://...실제경로...
DB_USERNAME=avnadmin
DB_PASSWORD=실제비밀번호
```

#### `application.properties`

```properties
spring.application.name=walking-service
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

> `ddl-auto=validate`: 테이블은 이미 SQL로 직접 만들었으므로, JPA는 검증만 한다.

### 6.3 의존성 (build.gradle)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## 7. 구현 순서 (로드맵)

> Claude Code에서 작업할 때 이 순서를 따른다.

### Phase 1 — 환경 세팅

- [ ] Spring Initializr로 프로젝트 생성 (Spring Web + JPA + MySQL Driver + Lombok)
- [ ] `.env` 파일 생성, `.gitignore`에 `.env` 추가
- [ ] `application.properties` 환경변수 참조 설정

### Phase 2 — DB 스키마 생성 (이미 완료된 상태)

- [x] `level_code`, `region` 코드 테이블 생성
- [x] `course` 테이블 생성
- [x] `app_user`, `favorite`, `review`, `walk_log` 테이블 생성
- [x] `fn_distance_km` 함수 등록

### Phase 3 — CSV 데이터 적재 (이미 완료된 상태)

- [x] `load_data.sql` 실행

### Phase 4 — Entity 클래스 작성

- [ ] `Course.java` — `@Entity`, `@Table(name = "course")`
- [ ] `Region.java`
- [ ] `LevelCode.java`
- [ ] `AppUser.java`
- [ ] `Favorite.java` + `FavoriteId.java` (복합키 `@IdClass`)
- [ ] `Review.java`
- [ ] `WalkLog.java`
- [ ] 서버 기동 → `ddl-auto=validate` 통과 확인

### Phase 5 — Repository 인터페이스

- [ ] `CourseRepository` — 조건검색 쿼리, 주변 코스 native query
- [ ] `FavoriteRepository` — 사용자별 조회
- [ ] `ReviewRepository` — 코스별 평균평점
- [ ] `WalkLogRepository` — 통계 집계 쿼리

### Phase 6 — Service 계층

- [ ] `CourseService` — 검색 로직, 상세 조회 + 평점 조합
- [ ] `FavoriteService` — 즐겨찾기 추가/해제 토글
- [ ] `ReviewService` — 작성 (중복 검증은 UNIQUE가 처리)
- [ ] `WalkStatsService` — 통계 집계 (횟수/거리/자주 간 코스)

### Phase 7 — Controller + DTO

- [ ] 각 엔드포인트 구현 (5.1 표 참고)
- [ ] Postman 또는 curl로 동작 확인

### Phase 8 — 최종 확인

- [ ] 전체 API 동작 테스트
- [ ] 오류 처리 (존재하지 않는 코스 ID, 잘못된 파라미터 등)
- [ ] 불필요한 코드 정리

---

## 8. 예시 쿼리 (참고)

### 조건 검색 — 경기도, 쉬움 이하, 5km 미만, 거리순

```sql
SELECT c.course_name, r.sido, r.sigungu, l.level_name, c.length_km
FROM   course c
JOIN   region r     ON c.region_id = r.region_id
JOIN   level_code l ON c.level_id  = l.level_id
WHERE  r.sido = '경기'
  AND  c.level_id <= 2
  AND  c.length_km < 5
ORDER BY c.length_km ASC;
```

### 코스별 평균 별점

```sql
SELECT c.course_name, ROUND(AVG(rv.rating),2) AS avg_rating, COUNT(*) AS n
FROM   course c JOIN review rv ON c.course_id = rv.course_id
GROUP BY c.course_id, c.course_name
HAVING COUNT(*) >= 3
ORDER BY avg_rating DESC;
```

### 내 주변 코스 5개

```sql
SELECT course_name, fn_distance_km(35.87, 128.60, lat, lon) AS dist_km
FROM   course
ORDER BY dist_km ASC
LIMIT 5;
```

---

## 9. 참고 결정사항 로그

| 결정 | 이유 |
|------|------|
| Flyway 미사용 | 혼자 하는 과제에서 오히려 설정 복잡도만 증가 |
| Lombok 사용 | Entity 보일러플레이트 감소, 선택사항 |
| stored program은 fn_distance_km만 | 통계는 Service로 이동. DB에 남기는 건 데이터 밀착 연산만 |
| time_min NULL 허용 | 파싱 불가 케이스 5.8%는 무리하게 추정하지 않음 |
| `ddl-auto=validate` | 테이블은 SQL로 직접 생성, JPA는 검증만 |
| `.env` + EnvFile 플러그인 | 비밀번호 Git 노출 방지 |
