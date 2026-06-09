package com.example.walking.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbc;

    public ReviewRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 후기 작성. 코스당 1인 1리뷰(UNIQUE) 위반 시 DuplicateKeyException 발생 →
     * Service/예외처리에서 409 로 매핑.
     */
    public int insert(int userId, String courseId, int rating, String comment) {
        return jdbc.update(
                "INSERT INTO review (user_id, course_id, rating, comment) VALUES (?, ?, ?, ?)",
                userId, courseId, rating, comment);
    }
}
