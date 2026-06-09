package com.example.walking.service;

import com.example.walking.dto.WalkStatsDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalkStatsService {

    private final JdbcTemplate jdbc;

    public WalkStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public WalkStatsDto getMyStats(int userId) {
        WalkStatsDto.Summary summary = jdbc.queryForObject("""
                SELECT COUNT(*)                              AS walk_count,
                       ROUND(COALESCE(SUM(c.length_km),0),1) AS total_dist,
                       COALESCE(SUM(w.spent_min), 0)         AS total_min
                FROM   walk_log w
                JOIN   course c ON w.course_id = c.course_id
                WHERE  w.user_id = ?
                """,
                (rs, n) -> new WalkStatsDto.Summary(
                        rs.getLong("walk_count"),
                        rs.getBigDecimal("total_dist"),
                        rs.getLong("total_min")),
                userId);

        List<WalkStatsDto.TopCourse> top3 = jdbc.query("""
                SELECT w.course_id, c.course_name, COUNT(*) AS visit_count
                FROM   walk_log w
                JOIN   course c ON w.course_id = c.course_id
                WHERE  w.user_id = ?
                GROUP  BY w.course_id, c.course_name
                ORDER  BY visit_count DESC, c.course_name ASC
                LIMIT  3
                """,
                (rs, n) -> new WalkStatsDto.TopCourse(
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getLong("visit_count")),
                userId);

        if (summary == null) {
            summary = new WalkStatsDto.Summary(0L, BigDecimal.ZERO, 0L);
        }
        return new WalkStatsDto(summary.walkCount(), summary.totalDist(), summary.totalMin(), top3);
    }
}
