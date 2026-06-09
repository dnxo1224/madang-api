package com.example.walking.dto;

import java.math.BigDecimal;

/** 내 주변 코스 (GET /api/courses/nearby). distKm 은 DB fn_distance_km 결과 */
public class NearbyCourseDto {
    private String courseId;
    private String courseName;
    private String sido;
    private String sigungu;
    private String levelName;
    private BigDecimal lengthKm;
    private BigDecimal distKm;

    public NearbyCourseDto(String courseId, String courseName, String sido, String sigungu,
                           String levelName, BigDecimal lengthKm, BigDecimal distKm) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.sido = sido;
        this.sigungu = sigungu;
        this.levelName = levelName;
        this.lengthKm = lengthKm;
        this.distKm = distKm;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }
    public String getLevelName() { return levelName; }
    public BigDecimal getLengthKm() { return lengthKm; }
    public BigDecimal getDistKm() { return distKm; }
}
