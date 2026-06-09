-- =============================================================
-- Stored Procedures (3개)
-- MySQL Workbench 실행 방법:
--   이 파일 전체를 열고 상단 번개 모양(Execute Script) 버튼 클릭
--   또는 File > Run SQL Script 사용
-- =============================================================

DROP PROCEDURE IF EXISTS sp_toggle_favorite;
DROP PROCEDURE IF EXISTS sp_add_review;
DROP PROCEDURE IF EXISTS sp_user_walk_stats;

DELIMITER $$

-- ------------------------------------------------------------
-- 1. sp_toggle_favorite
--    즐겨찾기가 없으면 추가(ADDED), 있으면 해제(REMOVED)
--    p_action OUT 파라미터로 결과 반환
-- ------------------------------------------------------------
CREATE PROCEDURE sp_toggle_favorite(
    IN  p_user_id   INT,
    IN  p_course_id VARCHAR(20),
    OUT p_action    VARCHAR(10)
)
BEGIN
    IF EXISTS (
        SELECT 1 FROM favorite
        WHERE user_id = p_user_id AND course_id = p_course_id
    ) THEN
        DELETE FROM favorite
        WHERE  user_id = p_user_id AND course_id = p_course_id;
        SET p_action = 'REMOVED';
    ELSE
        INSERT INTO favorite (user_id, course_id)
        VALUES (p_user_id, p_course_id);
        SET p_action = 'ADDED';
    END IF;
END $$

-- ------------------------------------------------------------
-- 2. sp_add_review
--    사용자/코스 존재 검증 + 중복 리뷰 검증 후 INSERT
--    p_result OUT 파라미터: OK / USER_NOT_FOUND /
--                           COURSE_NOT_FOUND / ALREADY_REVIEWED
-- ------------------------------------------------------------
CREATE PROCEDURE sp_add_review(
    IN  p_user_id   INT,
    IN  p_course_id VARCHAR(20),
    IN  p_rating    TINYINT,
    IN  p_comment   VARCHAR(500),
    OUT p_result    VARCHAR(20)
)
BEGIN
    DECLARE v_user_exists      INT DEFAULT 0;
    DECLARE v_course_exists    INT DEFAULT 0;
    DECLARE v_already_reviewed INT DEFAULT 0;

    SELECT COUNT(*) INTO v_user_exists
        FROM app_user WHERE user_id = p_user_id;

    SELECT COUNT(*) INTO v_course_exists
        FROM course WHERE course_id = p_course_id;

    SELECT COUNT(*) INTO v_already_reviewed
        FROM review
        WHERE user_id = p_user_id AND course_id = p_course_id;

    IF v_user_exists = 0 THEN
        SET p_result = 'USER_NOT_FOUND';
    ELSEIF v_course_exists = 0 THEN
        SET p_result = 'COURSE_NOT_FOUND';
    ELSEIF v_already_reviewed > 0 THEN
        SET p_result = 'ALREADY_REVIEWED';
    ELSE
        INSERT INTO review (user_id, course_id, rating, comment)
        VALUES (p_user_id, p_course_id, p_rating, p_comment);
        SET p_result = 'OK';
    END IF;
END $$

-- ------------------------------------------------------------
-- 3. sp_user_walk_stats
--    사용자 산책 통계 집계
--    OUT 파라미터: 총 횟수 / 누적 거리 / 누적 시간
--    Result Set  : 자주 간 코스 Top 3
-- ------------------------------------------------------------
CREATE PROCEDURE sp_user_walk_stats(
    IN  p_user_id       INT,
    OUT p_walk_count    INT,
    OUT p_total_dist_km DECIMAL(8,1),
    OUT p_total_min     INT
)
BEGIN
    -- 요약 집계 → OUT 파라미터
    SELECT
        COUNT(*),
        ROUND(COALESCE(SUM(c.length_km), 0), 1),
        COALESCE(SUM(w.spent_min), 0)
    INTO p_walk_count, p_total_dist_km, p_total_min
    FROM walk_log w
    JOIN course c ON w.course_id = c.course_id
    WHERE w.user_id = p_user_id;

    -- 자주 간 코스 Top 3 → Result Set (앱에서 읽음)
    SELECT w.course_id, c.course_name, COUNT(*) AS visit_count
    FROM   walk_log w
    JOIN   course c ON w.course_id = c.course_id
    WHERE  w.user_id = p_user_id
    GROUP  BY w.course_id, c.course_name
    ORDER  BY visit_count DESC, c.course_name ASC
    LIMIT  3;
END $$

DELIMITER ;

-- 등록 확인
SHOW PROCEDURE STATUS WHERE Db = DATABASE();
