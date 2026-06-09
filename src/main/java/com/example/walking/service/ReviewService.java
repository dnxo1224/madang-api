package com.example.walking.service;

import com.example.walking.dto.ReviewRequestDto;
import com.example.walking.exception.BadRequestException;
import com.example.walking.exception.ConflictException;
import com.example.walking.exception.NotFoundException;
import com.example.walking.repository.CourseRepository;
import com.example.walking.repository.ReviewRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final CourseRepository courseRepo;
    private final JdbcTemplate jdbc;

    public ReviewService(ReviewRepository reviewRepo,
                         CourseRepository courseRepo,
                         JdbcTemplate jdbc) {
        this.reviewRepo = reviewRepo;
        this.courseRepo = courseRepo;
        this.jdbc       = jdbc;
    }

    public void create(String courseId, ReviewRequestDto req) {
        if (req.getUserId() == null) {
            throw new BadRequestException("userId 는 필수입니다.");
        }
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new BadRequestException("rating 은 1~5 사이여야 합니다.");
        }

        Integer userCnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE user_id = ?",
                Integer.class, req.getUserId());
        if (userCnt == null || userCnt == 0) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }
        if (!courseRepo.existsById(courseId)) {
            throw new NotFoundException("코스를 찾을 수 없습니다: " + courseId);
        }

        try {
            reviewRepo.insert(req.getUserId(), courseId, req.getRating(), req.getComment());
        } catch (DuplicateKeyException e) {
            throw new ConflictException("이미 이 코스에 작성한 리뷰가 있습니다.");
        }
    }
}
