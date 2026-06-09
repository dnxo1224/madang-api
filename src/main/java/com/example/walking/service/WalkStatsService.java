package com.example.walking.service;

import com.example.walking.dto.WalkStatsDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** 나의 산책 통계 — sp_user_walk_stats 프로시저 호출 */
@Service
public class WalkStatsService {

    private final JdbcTemplate jdbc;

    public WalkStatsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * sp_user_walk_stats 프로시저 호출.
     * OUT 파라미터: 총 횟수, 누적 거리(km), 누적 시간(분)
     * Result Set : 자주 간 코스 Top 3
     */
    public WalkStatsDto getMyStats(int userId) {
        return jdbc.execute(
                "{call sp_user_walk_stats(?, ?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setInt(1, userId);                          // IN  p_user_id
                    cs.registerOutParameter(2, Types.INTEGER);     // OUT p_walk_count
                    cs.registerOutParameter(3, Types.DECIMAL);     // OUT p_total_dist_km
                    cs.registerOutParameter(4, Types.INTEGER);     // OUT p_total_min
                    cs.execute();

                    long walkCount    = cs.getLong(2);
                    BigDecimal dist   = cs.getBigDecimal(3);
                    long totalMin     = cs.getLong(4);

                    // Result Set — 자주 간 코스 Top 3
                    List<WalkStatsDto.TopCourse> top3 = new ArrayList<>();
                    ResultSet rs = cs.getResultSet();
                    if (rs != null) {
                        while (rs.next()) {
                            top3.add(new WalkStatsDto.TopCourse(
                                    rs.getString("course_id"),
                                    rs.getString("course_name"),
                                    rs.getLong("visit_count")));
                        }
                    }

                    return new WalkStatsDto(
                            walkCount,
                            dist != null ? dist : BigDecimal.ZERO,
                            totalMin,
                            top3);
                });
    }
}
