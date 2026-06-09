package com.example.walking.dto;

import java.math.BigDecimal;

/** 검색 결과 목록 한 행 (GET /api/courses) */
public class CourseSummaryDto {
    private String courseId;
    private String courseName;
    private String sido;
    private String sigungu;
    private String levelName;
    private BigDecimal lengthKm;
    private Integer timeMin;

    public CourseSummaryDto(String courseId, String courseName, String sido, String sigungu,
                            String levelName, BigDecimal lengthKm, Integer timeMin) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.sido = sido;
        this.sigungu = sigungu;
        this.levelName = levelName;
        this.lengthKm = lengthKm;
        this.timeMin = timeMin;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }
    public String getLevelName() { return levelName; }
    public BigDecimal getLengthKm() { return lengthKm; }
    public Integer getTimeMin() { return timeMin; }
}
