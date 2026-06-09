-- =============================================================
-- Stored Procedure — sp_refresh_ranking
-- course_ranking 테이블을 두 가지 기준으로 갱신한다.
--   ACTIVITY : 즐겨찾기 + 리뷰 수 합산 TOP 5
--   RATING   : 평균 평점 TOP 5 (리뷰 1건 이상인 코스만)
-- 호출 주체: Spring @Scheduled (매일 09:00)
-- 실행 방법: MySQL Workbench → Execute Script(번개 아이콘) 또는 Run SQL Script
-- =============================================================

DROP PROCEDURE IF EXISTS sp_refresh_ranking;

DELIMITER $$

CREATE PROCEDURE sp_refresh_ranking()
BEGIN
    -- --------------------------------------------------------
    -- ACTIVITY 랭킹: 즐겨찾기 + 리뷰 수 합산 TOP 5
    -- --------------------------------------------------------
    DELETE FROM course_ranking WHERE rank_type = 'ACTIVITY';

    INSERT INTO course_ranking (rank_type, rank_no, course_id, score, refreshed_at)
    SELECT 'ACTIVITY',
           ROW_NUMBER() OVER (ORDER BY cnt DESC, course_id ASC) AS rank_no,
           course_id,
           cnt,
           NOW()
    FROM (
        SELECT course_id, COUNT(*) AS cnt
        FROM (
            SELECT course_id FROM favorite
            UNION ALL
            SELECT course_id FROM review
        ) combined
        GROUP BY course_id
        ORDER BY cnt DESC, course_id ASC
        LIMIT 5
    ) t;

    -- --------------------------------------------------------
    -- RATING 랭킹: 평균 평점 TOP 5
    -- --------------------------------------------------------
    DELETE FROM course_ranking WHERE rank_type = 'RATING';

    INSERT INTO course_ranking (rank_type, rank_no, course_id, score, refreshed_at)
    SELECT 'RATING',
           ROW_NUMBER() OVER (ORDER BY avg_rating DESC, course_id ASC) AS rank_no,
           course_id,
           avg_rating,
           NOW()
    FROM (
        SELECT course_id, ROUND(AVG(rating), 2) AS avg_rating
        FROM review
        GROUP BY course_id
        ORDER BY avg_rating DESC, course_id ASC
        LIMIT 5
    ) t;
END $$

DELIMITER ;

-- 등록 확인
SHOW PROCEDURE STATUS WHERE Db = DATABASE();
