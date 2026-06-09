package com.example.walking.repository;

import com.example.walking.dto.CourseSummaryDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FavoriteRepository {

    private final JdbcTemplate jdbc;

    public FavoriteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean exists(int userId, String courseId) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM favorite WHERE user_id = ? AND course_id = ?",
                Integer.class, userId, courseId);
        return cnt != null && cnt > 0;
    }

    public void add(int userId, String courseId) {
        jdbc.update("INSERT INTO favorite (user_id, course_id) VALUES (?, ?)", userId, courseId);
    }

    public int remove(int userId, String courseId) {
        return jdbc.update("DELETE FROM favorite WHERE user_id = ? AND course_id = ?",
                userId, courseId);
    }

    /** 사용자별 즐겨찾기 코스 목록 */
    public List<CourseSummaryDto> findByUser(int userId) {
        String sql = """
                SELECT c.course_id, c.course_name, r.sido, r.sigungu,
                       l.level_name, c.length_km, c.time_min
                FROM   favorite f
                JOIN   course c     ON f.course_id = c.course_id
                JOIN   region r     ON c.region_id = r.region_id
                JOIN   level_code l ON c.level_id  = l.level_id
                WHERE  f.user_id = ?
                ORDER BY f.added_at DESC
                """;
        return jdbc.query(sql, (rs, n) -> new CourseSummaryDto(
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("sido"),
                rs.getString("sigungu"),
                rs.getString("level_name"),
                rs.getBigDecimal("length_km"),
                (Integer) rs.getObject("time_min")), userId);
    }
}
