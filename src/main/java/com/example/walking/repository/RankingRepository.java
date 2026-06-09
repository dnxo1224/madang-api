package com.example.walking.repository;

import com.example.walking.dto.RankingDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RankingRepository {

    private final JdbcTemplate jdbc;

    public RankingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<RankingDto.Item> findItems(String rankType) {
        return jdbc.query("""
                SELECT cr.rank_no, cr.course_id, c.course_name, cr.score
                FROM   course_ranking cr
                JOIN   course c ON cr.course_id = c.course_id
                WHERE  cr.rank_type = ?
                ORDER  BY cr.rank_no
                """,
                (rs, n) -> new RankingDto.Item(
                        rs.getInt("rank_no"),
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getBigDecimal("score")),
                rankType);
    }

    public LocalDateTime findRefreshedAt(String rankType) {
        return jdbc.query("""
                SELECT refreshed_at FROM course_ranking
                WHERE  rank_type = ? LIMIT 1
                """,
                (rs, n) -> rs.getTimestamp("refreshed_at").toLocalDateTime(),
                rankType)
                .stream().findFirst().orElse(null);
    }
}
