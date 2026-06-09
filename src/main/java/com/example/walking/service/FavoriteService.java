package com.example.walking.service;

import com.example.walking.exception.NotFoundException;
import com.example.walking.repository.CourseRepository;
import com.example.walking.repository.FavoriteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Types;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepo;
    private final CourseRepository courseRepo;
    private final JdbcTemplate jdbc;

    public FavoriteService(FavoriteRepository favoriteRepo,
                           CourseRepository courseRepo,
                           JdbcTemplate jdbc) {
        this.favoriteRepo = favoriteRepo;
        this.courseRepo   = courseRepo;
        this.jdbc         = jdbc;
    }

    /** 즐겨찾기 추가 (이미 있으면 멱등 처리) */
    @Transactional
    public void add(int userId, String courseId) {
        requireCourse(courseId);
        if (!favoriteRepo.exists(userId, courseId)) {
            favoriteRepo.add(userId, courseId);
        }
    }

    /** 즐겨찾기 해제 */
    @Transactional
    public void remove(int userId, String courseId) {
        requireCourse(courseId);
        favoriteRepo.remove(userId, courseId);
    }

    /**
     * 즐겨찾기 토글 — sp_toggle_favorite 프로시저 호출.
     * 없으면 추가 후 "ADDED", 있으면 삭제 후 "REMOVED" 반환.
     */
    @Transactional
    public String toggle(int userId, String courseId) {
        requireCourse(courseId);
        return jdbc.execute(
                "{call sp_toggle_favorite(?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setInt(1, userId);        // IN  p_user_id
                    cs.setString(2, courseId);   // IN  p_course_id
                    cs.registerOutParameter(3, Types.VARCHAR); // OUT p_action
                    cs.execute();
                    return cs.getString(3);      // 'ADDED' 또는 'REMOVED'
                });
    }

    private void requireCourse(String courseId) {
        if (!courseRepo.existsById(courseId)) {
            throw new NotFoundException("코스를 찾을 수 없습니다: " + courseId);
        }
    }
}
