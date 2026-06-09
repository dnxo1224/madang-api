package com.example.walking.repository;

import com.example.walking.dto.CourseDetailDto;
import com.example.walking.dto.CourseSearchDto;
import com.example.walking.dto.CourseSummaryDto;
import com.example.walking.dto.NearbyCourseDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourseRepository {

    private final JdbcTemplate jdbc;

    public CourseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<CourseSummaryDto> SUMMARY_MAPPER = (rs, n) -> new CourseSummaryDto(
            rs.getString("course_id"),
            rs.getString("course_name"),
            rs.getString("sido"),
            rs.getString("sigungu"),
            rs.getString("level_name"),
            rs.getBigDecimal("length_km"),
            (Integer) rs.getObject("time_min"));

    /** 조건검색 — 지역/난이도/거리 동적 필터 + 정렬 */
    public List<CourseSummaryDto> search(CourseSearchDto criteria) {
        StringBuilder sql = new StringBuilder("""
                SELECT c.course_id, c.course_name, r.sido, r.sigungu,
                       l.level_name, c.length_km, c.time_min
                FROM   course c
                JOIN   region r     ON c.region_id = r.region_id
                JOIN   level_code l ON c.level_id  = l.level_id
                WHERE  1 = 1
                """);
        List<Object> params = new ArrayList<>();

        if (criteria.getSido() != null && !criteria.getSido().isBlank()) {
            sql.append(" AND r.sido = ?");
            params.add(criteria.getSido());
        }
        if (criteria.getMaxLevel() != null) {
            sql.append(" AND c.level_id <= ?");
            params.add(criteria.getMaxLevel());
        }
        if (criteria.getMaxLength() != null) {
            sql.append(" AND c.length_km < ?");
            params.add(criteria.getMaxLength());
        }

        // 정렬 — 화이트리스트로만 결정 (SQL injection 방지)
        if ("length".equals(criteria.getSort())) {
            sql.append(" ORDER BY c.length_km IS NULL, c.length_km ASC");
        } else if ("time".equals(criteria.getSort())) {
            sql.append(" ORDER BY c.time_min IS NULL, c.time_min ASC");
        } else {
            sql.append(" ORDER BY c.course_name ASC");
        }
        sql.append(" LIMIT 200");

        return jdbc.query(sql.toString(), SUMMARY_MAPPER, params.toArray());
    }

    /** 내 주변 코스 — DB stored function fn_distance_km 으로 거리 계산·정렬·LIMIT */
    public List<NearbyCourseDto> findNearby(double lat, double lon, int limit) {
        String sql = """
                SELECT c.course_id, c.course_name, r.sido, r.sigungu, l.level_name,
                       c.length_km, fn_distance_km(?, ?, c.lat, c.lon) AS dist_km
                FROM   course c
                JOIN   region r     ON c.region_id = r.region_id
                JOIN   level_code l ON c.level_id  = l.level_id
                ORDER BY dist_km ASC
                LIMIT ?
                """;
        return jdbc.query(sql, (rs, n) -> new NearbyCourseDto(
                rs.getString("course_id"),
                rs.getString("course_name"),
                rs.getString("sido"),
                rs.getString("sigungu"),
                rs.getString("level_name"),
                rs.getBigDecimal("length_km"),
                rs.getBigDecimal("dist_km")), lat, lon, limit);
    }

    /** 코스 상세 + 평균평점/리뷰수 (LEFT JOIN 집계) */
    public Optional<CourseDetailDto> findDetail(String courseId) {
        String sql = """
                SELECT c.course_id, c.course_name, c.flag_name, r.sido, r.sigungu,
                       l.level_name, c.length_km, c.time_min, c.toilet_info, c.store_info,
                       c.lat, c.lon, c.description,
                       AVG(rv.rating) AS avg_rating, COUNT(rv.review_id) AS review_count
                FROM   course c
                JOIN   region r     ON c.region_id = r.region_id
                JOIN   level_code l ON c.level_id  = l.level_id
                LEFT JOIN review rv ON rv.course_id = c.course_id
                WHERE  c.course_id = ?
                GROUP BY c.course_id, c.course_name, c.flag_name, r.sido, r.sigungu,
                         l.level_name, c.length_km, c.time_min, c.toilet_info, c.store_info,
                         c.lat, c.lon, c.description
                """;
        List<CourseDetailDto> result = jdbc.query(sql, (rs, n) -> {
            Double avg = (Double) rs.getObject("avg_rating");
            return new CourseDetailDto(
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getString("flag_name"),
                    rs.getString("sido"),
                    rs.getString("sigungu"),
                    rs.getString("level_name"),
                    rs.getBigDecimal("length_km"),
                    (Integer) rs.getObject("time_min"),
                    rs.getString("toilet_info"),
                    rs.getString("store_info"),
                    rs.getBigDecimal("lat"),
                    rs.getBigDecimal("lon"),
                    rs.getString("description"),
                    avg == null ? null : Math.round(avg * 100.0) / 100.0,
                    rs.getLong("review_count"));
        }, courseId);
        return result.stream().findFirst();
    }

    public boolean existsById(String courseId) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM course WHERE course_id = ?", Integer.class, courseId);
        return cnt != null && cnt > 0;
    }
}
