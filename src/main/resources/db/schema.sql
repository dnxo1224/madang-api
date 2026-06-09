-- =============================================================
-- 산책로 기록 서비스 — 스키마 DDL
-- 명세서 walking_service_spec.md §3.2 ~ §3.4 기준
-- 외부 Aiven MySQL 8.0 에서 실행
-- 실행 순서: schema.sql → function.sql → load_data.sql
-- =============================================================

-- 재실행 가능하도록 자식 → 부모 순으로 DROP (FK 역순)
DROP TABLE IF EXISTS walk_log;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS course_ranking;
DROP TABLE IF EXISTS app_user;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS region;
DROP TABLE IF EXISTS level_code;

-- ----------------------------------------------------
-- 코드 테이블
-- ----------------------------------------------------
CREATE TABLE level_code (
    level_id   TINYINT     PRIMARY KEY,        -- 1=매우쉬움 … 5=매우어려움
    level_name VARCHAR(10) NOT NULL UNIQUE
);

INSERT INTO level_code (level_id, level_name) VALUES
    (1, '매우쉬움'), (2, '쉬움'), (3, '보통'), (4, '어려움'), (5, '매우어려움');

CREATE TABLE region (
    region_id INT AUTO_INCREMENT PRIMARY KEY,
    sido      VARCHAR(20) NOT NULL,
    sigungu   VARCHAR(40) NOT NULL,
    UNIQUE (sido, sigungu)
);

-- ----------------------------------------------------
-- course (원천 정제 데이터)
-- ----------------------------------------------------
CREATE TABLE course (
    course_id   VARCHAR(20)   PRIMARY KEY,     -- 원천 ESNTL_ID
    course_name VARCHAR(100)  NOT NULL,
    flag_name   VARCHAR(100),                  -- 상위 길(해파랑길 등)
    region_id   INT           NOT NULL,
    level_id    TINYINT       NOT NULL,
    length_km   DECIMAL(5,1),                  -- 텍스트 → 수치 파생
    time_min    INT,                           -- '1시간 30분' → 90 파생
    toilet_info VARCHAR(200),                  -- 결측 허용
    store_info  VARCHAR(200),                  -- 결측 허용
    lat         DECIMAL(10,7) NOT NULL,
    lon         DECIMAL(10,7) NOT NULL,
    description TEXT,
    FOREIGN KEY (region_id) REFERENCES region(region_id),
    FOREIGN KEY (level_id)  REFERENCES level_code(level_id),
    CHECK (length_km IS NULL OR length_km >= 0),
    CHECK (time_min  IS NULL OR time_min  >= 0)
);

-- ----------------------------------------------------
-- 사용자 기능 테이블
-- ----------------------------------------------------
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
    UNIQUE (user_id, course_id),               -- 코스당 1인 1리뷰
    FOREIGN KEY (user_id)   REFERENCES app_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
);

-- ----------------------------------------------------
-- 랭킹 캐시 테이블 (매일 9시 sp_refresh_ranking 이 갱신)
-- ----------------------------------------------------
CREATE TABLE course_ranking (
    rank_type    VARCHAR(20)   NOT NULL,   -- 'ACTIVITY' | 'RATING'
    rank_no      TINYINT       NOT NULL,   -- 1 ~ 5
    course_id    VARCHAR(20)   NOT NULL,
    score        DECIMAL(8,2),            -- 활동 합산 수 또는 평균 평점
    refreshed_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rank_type, rank_no),
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

-- ----------------------------------------------------
-- 과제용 테스트 사용자 (인증 미구현 → userId 파라미터로 사용)
-- ----------------------------------------------------
INSERT INTO app_user (login_id, nickname) VALUES
    ('tester1', '산책왕'),
    ('tester2', '걷기달인');
