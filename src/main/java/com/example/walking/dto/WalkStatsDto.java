package com.example.walking.dto;

import java.math.BigDecimal;
import java.util.List;

/** 나의 산책 통계 (GET /api/users/me/stats) — 앱 계층 집계 */
public class WalkStatsDto {
    private long walkCount;             // 총 산책 횟수
    private BigDecimal totalDistanceKm; // 누적 거리(코스 length_km 합)
    private long totalMinutes;          // 누적 소요시간(spent_min 합)
    private List<TopCourse> topCourses; // 자주 간 코스

    public WalkStatsDto(long walkCount, BigDecimal totalDistanceKm,
                        long totalMinutes, List<TopCourse> topCourses) {
        this.walkCount = walkCount;
        this.totalDistanceKm = totalDistanceKm;
        this.totalMinutes = totalMinutes;
        this.topCourses = topCourses;
    }

    public long getWalkCount() { return walkCount; }
    public BigDecimal getTotalDistanceKm() { return totalDistanceKm; }
    public long getTotalMinutes() { return totalMinutes; }
    public List<TopCourse> getTopCourses() { return topCourses; }

    /** 자주 간 코스 한 행 */
    public static class TopCourse {
        private String courseId;
        private String courseName;
        private long visitCount;

        public TopCourse(String courseId, String courseName, long visitCount) {
            this.courseId = courseId;
            this.courseName = courseName;
            this.visitCount = visitCount;
        }

        public String getCourseId() { return courseId; }
        public String getCourseName() { return courseName; }
        public long getVisitCount() { return visitCount; }
    }
}
