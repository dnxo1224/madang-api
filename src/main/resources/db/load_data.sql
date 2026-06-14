-- =============================================================
-- CSV 데이터 적재 (명세서 §4)
-- 원본: 내_주변_산책로_데이터.csv  (UTF-8 BOM, 1,623행, 16컬럼)
-- 실행 전제: schema.sql, function.sql 먼저 실행됨
--
-- ★ 사전 준비 (LOAD DATA LOCAL INFILE 사용 시) ★
--   서버:   SET GLOBAL local_infile = 1;   (Aiven는 권한 제한될 수 있음)
--   클라이언트(mysql CLI): mysql --local-infile=1 ...
--   ★ 아래 '<<<CSV_PATH>>>' 를 본인 PC의 CSV 절대경로로 교체.
--      Windows 예: 'C:/Users/dnxo1/Downloads/내_주변_산책로_데이터.csv'
--      (역슬래시 \ 대신 슬래시 / 사용)
-- =============================================================

-- STEP 1. 원천 컬럼 그대로 받는 임시 테이블
DROP TABLE IF EXISTS raw_course;
CREATE TABLE raw_course (
    ESNTL_ID            VARCHAR(30) PRIMARY KEY,
    WLK_COURS_FLAG_NM   VARCHAR(100),
    WLK_COURS_NM        VARCHAR(100),
    COURS_DC            TEXT,
    SIGNGU_NM           VARCHAR(60),
    COURS_LEVEL_NM      VARCHAR(20),
    COURS_LT_CN         VARCHAR(50),
    COURS_DETAIL_LT_CN  VARCHAR(50),
    ADIT_DC             TEXT,
    COURS_TIME_CN       VARCHAR(50),
    OPTN_DC             TEXT,
    TOILET_DC           VARCHAR(200),
    CVNTL_NM            VARCHAR(200),
    LNM_ADDR            VARCHAR(200),
    COURS_SPOT_LA       VARCHAR(30),
    COURS_SPOT_LO       VARCHAR(30)
);

-- STEP 2. CSV → raw_course (UTF-8 BOM 헤더 1줄 스킵)
LOAD DATA LOCAL INFILE 'C:/Users/dnxo1/Downloads/내_주변_산책로_데이터.csv'
INTO TABLE raw_course
CHARACTER SET utf8mb4
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'     -- 파일 확인 결과 LF 전용 (CRLF 없음)
IGNORE 1 LINES
(ESNTL_ID, WLK_COURS_FLAG_NM, WLK_COURS_NM, COURS_DC, SIGNGU_NM,
 COURS_LEVEL_NM, COURS_LT_CN, COURS_DETAIL_LT_CN, ADIT_DC, COURS_TIME_CN,
 OPTN_DC, TOILET_DC, CVNTL_NM, LNM_ADDR, COURS_SPOT_LA, COURS_SPOT_LO);

-- BOM이 첫 컬럼에 붙는 경우 제거 (﻿)
UPDATE raw_course SET ESNTL_ID = REPLACE(ESNTL_ID, '﻿', '');

-- STEP 3. region 채우기 — SIGNGU_NM 공백 분리 (2단어 / 3단어), 중복 무시
--   2단어: '충남 청양군'         → sido='충남',  sigungu='청양군'
--   3단어: '경기 고양시 덕양구'  → sido='경기',  sigungu='고양시 덕양구'
INSERT IGNORE INTO region (sido, sigungu)
SELECT
    SUBSTRING_INDEX(TRIM(SIGNGU_NM), ' ', 1) AS sido,
    TRIM(SUBSTRING(TRIM(SIGNGU_NM),
        LENGTH(SUBSTRING_INDEX(TRIM(SIGNGU_NM), ' ', 1)) + 2)) AS sigungu
FROM raw_course
WHERE SIGNGU_NM IS NOT NULL
  AND TRIM(SIGNGU_NM) <> ''
  AND LOCATE(' ', TRIM(SIGNGU_NM)) > 0;

-- STEP 4. course 채우기 — region/level_code 조인, length_km 파싱
--   COURS_DETAIL_LT_CN 이 순수 실수 문자열이면 수치, 아니면 NULL
INSERT INTO course
    (course_id, course_name, flag_name, region_id, level_id,
     length_km, time_min, toilet_info, store_info, lat, lon, description)
SELECT
    rc.ESNTL_ID,
    rc.WLK_COURS_NM,
    NULLIF(TRIM(rc.WLK_COURS_FLAG_NM), ''),
    r.region_id,
    COALESCE(lc.level_id, 3),                       -- 매칭 안 되면 '보통'
    CASE WHEN rc.COURS_DETAIL_LT_CN REGEXP '^[0-9]+(\\.[0-9]+)?$'
         THEN CAST(rc.COURS_DETAIL_LT_CN AS DECIMAL(5,1)) END,
    NULL,                                           -- time_min 은 STEP 5에서 채움
    NULLIF(TRIM(rc.TOILET_DC), ''),
    NULLIF(TRIM(rc.CVNTL_NM), ''),
    CAST(rc.COURS_SPOT_LA AS DECIMAL(10,7)),
    CAST(rc.COURS_SPOT_LO AS DECIMAL(10,7)),
    NULLIF(TRIM(rc.COURS_DC), '')
FROM raw_course rc
JOIN region r
      ON r.sido    = SUBSTRING_INDEX(TRIM(rc.SIGNGU_NM), ' ', 1)
     AND r.sigungu = TRIM(SUBSTRING(TRIM(rc.SIGNGU_NM),
                     LENGTH(SUBSTRING_INDEX(TRIM(rc.SIGNGU_NM), ' ', 1)) + 2))
LEFT JOIN level_code lc
      ON lc.level_name = TRIM(rc.COURS_LEVEL_NM)
WHERE rc.COURS_SPOT_LA REGEXP '^[0-9.]+$'
  AND rc.COURS_SPOT_LO REGEXP '^[0-9.]+$';

-- STEP 5. time_min UPDATE — 4패턴 순차 적용 (명세서 §2.3, 94.2% 커버)
--   raw_course 의 COURS_TIME_CN 을 course 와 조인해 변환.
-- 5-1. 'N시간 M분' / 'N시간M분' (앞에 '약' 허용)
UPDATE course c
JOIN raw_course rc ON rc.ESNTL_ID = c.course_id
SET c.time_min =
    CAST(REGEXP_SUBSTR(rc.COURS_TIME_CN, '[0-9]+') AS UNSIGNED) * 60
  + CAST(REGEXP_SUBSTR(rc.COURS_TIME_CN, '[0-9]+(?=분)') AS UNSIGNED)
WHERE rc.COURS_TIME_CN REGEXP '[0-9]+시간[ ]?[0-9]+분';

-- 5-2. 'N시간'
UPDATE course c
JOIN raw_course rc ON rc.ESNTL_ID = c.course_id
SET c.time_min = CAST(REGEXP_SUBSTR(rc.COURS_TIME_CN, '[0-9]+') AS UNSIGNED) * 60
WHERE c.time_min IS NULL
  AND rc.COURS_TIME_CN REGEXP '[0-9]+시간'
  AND rc.COURS_TIME_CN NOT REGEXP '분'
  AND rc.COURS_TIME_CN NOT REGEXP '왕복|~|박';

-- 5-3. 'N분'
UPDATE course c
JOIN raw_course rc ON rc.ESNTL_ID = c.course_id
SET c.time_min = CAST(REGEXP_SUBSTR(rc.COURS_TIME_CN, '[0-9]+') AS UNSIGNED)
WHERE c.time_min IS NULL
  AND rc.COURS_TIME_CN REGEXP '[0-9]+분'
  AND rc.COURS_TIME_CN NOT REGEXP '시간';

-- 5-4. 순수 숫자 → 분으로 간주
UPDATE course c
JOIN raw_course rc ON rc.ESNTL_ID = c.course_id
SET c.time_min = CAST(rc.COURS_TIME_CN AS UNSIGNED)
WHERE c.time_min IS NULL
  AND TRIM(rc.COURS_TIME_CN) REGEXP '^[0-9]+$';
-- 그 외('4박 5일','왕복 3시간','3~4시간' 등)는 NULL 유지

-- STEP 6. 임시 테이블 제거
DROP TABLE IF EXISTS raw_course;

-- STEP 7. 적재 결과 확인
SELECT COUNT(*) AS course_cnt FROM course;                 -- 기대: ~1623 (좌표 결측 제외)
SELECT COUNT(*) AS region_cnt FROM region;
SELECT
    SUM(length_km IS NULL) AS length_null,
    SUM(time_min  IS NULL) AS time_null
FROM course;
SELECT course_name, fn_distance_km(35.87, 128.60, lat, lon) AS dist_km
FROM course ORDER BY dist_km LIMIT 5;
