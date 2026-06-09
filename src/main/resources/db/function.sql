-- =============================================================
-- Stored Function — fn_distance_km
-- DB에 남기는 유일한 stored program (명세서 §3.5)
-- 하버사인 거리(km) 계산. 1,623건을 앱으로 끌지 않고 DB에서 정렬·LIMIT 하기 위함.
--
-- 실행 도구에 따라 DELIMITER 지원 여부가 다름:
--  - mysql CLI / MySQL Workbench: 아래 DELIMITER 블록 그대로 실행
--  - DBeaver: DROP 문은 따로 실행하고, CREATE 블록은 DELIMITER 없이 본문만 실행
-- =============================================================

DROP FUNCTION IF EXISTS fn_distance_km;

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

-- 검증: 대구 근처 한 점에서 가까운 코스 5개
-- SELECT course_name, fn_distance_km(35.87, 128.60, lat, lon) AS dist_km
-- FROM course ORDER BY dist_km LIMIT 5;
