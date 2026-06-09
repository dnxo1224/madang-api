package com.example.walking.controller;

import com.example.walking.dto.*;
import com.example.walking.service.CourseService;
import com.example.walking.service.FavoriteService;
import com.example.walking.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final FavoriteService favoriteService;
    private final ReviewService reviewService;

    public CourseController(CourseService courseService,
                            FavoriteService favoriteService,
                            ReviewService reviewService) {
        this.courseService = courseService;
        this.favoriteService = favoriteService;
        this.reviewService = reviewService;
    }

    /** 조건검색 — 지역/난이도/거리/정렬 */
    @GetMapping
    public List<CourseSummaryDto> search(
            @RequestParam(required = false) String sido,
            @RequestParam(required = false) Integer maxLevel,
            @RequestParam(required = false) BigDecimal maxLength,
            @RequestParam(required = false) String sort) {
        CourseSearchDto criteria = new CourseSearchDto();
        criteria.setSido(sido);
        criteria.setMaxLevel(maxLevel);
        criteria.setMaxLength(maxLength);
        criteria.setSort(sort);
        return courseService.search(criteria);
    }

    /** 내 주변 코스 N개 */
    @GetMapping("/nearby")
    public List<NearbyCourseDto> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") int limit) {
        return courseService.findNearby(lat, lon, limit);
    }

    /** 코스 상세 + 평균평점 */
    @GetMapping("/{id}")
    public CourseDetailDto detail(@PathVariable("id") String courseId) {
        return courseService.getDetail(courseId);
    }

    /** 즐겨찾기 추가 */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> addFavorite(@PathVariable("id") String courseId,
                                            @RequestParam int userId) {
        favoriteService.add(userId, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 즐겨찾기 해제 */
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<Void> removeFavorite(@PathVariable("id") String courseId,
                                               @RequestParam int userId) {
        favoriteService.remove(userId, courseId);
        return ResponseEntity.noContent().build();
    }

    /** 후기 작성 (별점) */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Void> addReview(@PathVariable("id") String courseId,
                                          @RequestBody ReviewRequestDto req) {
        reviewService.create(courseId, req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
