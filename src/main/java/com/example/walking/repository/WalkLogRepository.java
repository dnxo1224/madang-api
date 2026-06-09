package com.example.walking.repository;

import com.example.walking.dto.WalkStatsDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public class WalkLogRepository {

    private final JdbcTemplate jdbc;

    public WalkLogRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insert(int userId, String courseId, LocalDate walkedOn, Integer spentMin) {
        return jdbc.update(
                "INSERT INTO walk_log (user_id, course_id, walked_on, spent_min) VALUES (?, ?, ?, ?)",
                userId, courseId, walkedOn, spentMin);
    }

    /**
     * 사용자 산책 통계 요약 — 횟수, 누적거리(코스 length_km 합), 누적 소요시간(spent_min 합).
     * 결과 키: walk_count, total_distance_km, total_minutes
     */
    public Map<String, Object> findSummary(int userId) {
        String sql = """
                SELECT COUNT(*)                          AS walk_count,
                       COALESCE(SUM(c.length_km), 0)     AS total_distance_km,
                       COALESCE(SUM(w.spent_min), 0)     AS total_minutes
                FROM   walk_log w
                JOIN   course c ON w.course_id = c.course_id
                WHERE  w.user_id = ?
                """;
        return jdbc.queryForMap(sql, userId);
    }

    /** 자주 간 코스 top N */
    public List<WalkStatsDto.TopCourse> findTopCourses(int userId, int limit) {
        String sql = """
                SELECT w.course_id, c.course_name, COUNT(*) AS visit_count
                FROM   walk_log w
                JOIN   course c ON w.course_id = c.course_id
                WHERE  w.user_id = ?
                GROUP BY w.course_id, c.course_name
                ORDER BY visit_count DESC, c.course_name ASC
                LIMIT ?
                """;
        return jdbc.query(sql, (rs, n) -> new WalkStatsDto.TopCourse(
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getLong("visit_count")), userId, limit);
    }

    public static BigDecimal asBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        return new BigDecimal(value.toString());
    }
}
