package com.example.walking.service;

import com.example.walking.exception.NotFoundException;
import com.example.walking.repository.CourseRepository;
import com.example.walking.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepo;
    private final CourseRepository courseRepo;

    public FavoriteService(FavoriteRepository favoriteRepo,
                           CourseRepository courseRepo) {
        this.favoriteRepo = favoriteRepo;
        this.courseRepo   = courseRepo;
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

    /** 즐겨찾기 토글 — 없으면 추가(ADDED), 있으면 해제(REMOVED) */
    @Transactional
    public String toggle(int userId, String courseId) {
        requireCourse(courseId);
        if (favoriteRepo.exists(userId, courseId)) {
            favoriteRepo.remove(userId, courseId);
            return "REMOVED";
        }
        favoriteRepo.add(userId, courseId);
        return "ADDED";
    }

    private void requireCourse(String courseId) {
        if (!courseRepo.existsById(courseId)) {
            throw new NotFoundException("코스를 찾을 수 없습니다: " + courseId);
        }
    }
}
