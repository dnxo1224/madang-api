package com.example.walking.dto;

import java.math.BigDecimal;

/** 코스 상세 + 평균평점 (GET /api/courses/{id}) */
public class CourseDetailDto {
    private String courseId;
    private String courseName;
    private String flagName;
    private String sido;
    private String sigungu;
    private String levelName;
    private BigDecimal lengthKm;
    private Integer timeMin;
    private String toiletInfo;
    private String storeInfo;
    private BigDecimal lat;
    private BigDecimal lon;
    private String description;
    private Double avgRating;     // 리뷰 없으면 null
    private long reviewCount;

    public CourseDetailDto(String courseId, String courseName, String flagName,
                           String sido, String sigungu, String levelName,
                           BigDecimal lengthKm, Integer timeMin,
                           String toiletInfo, String storeInfo,
                           BigDecimal lat, BigDecimal lon, String description,
                           Double avgRating, long reviewCount) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.flagName = flagName;
        this.sido = sido;
        this.sigungu = sigungu;
        this.levelName = levelName;
        this.lengthKm = lengthKm;
        this.timeMin = timeMin;
        this.toiletInfo = toiletInfo;
        this.storeInfo = storeInfo;
        this.lat = lat;
        this.lon = lon;
        this.description = description;
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getFlagName() { return flagName; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }
    public String getLevelName() { return levelName; }
    public BigDecimal getLengthKm() { return lengthKm; }
    public Integer getTimeMin() { return timeMin; }
    public String getToiletInfo() { return toiletInfo; }
    public String getStoreInfo() { return storeInfo; }
    public BigDecimal getLat() { return lat; }
    public BigDecimal getLon() { return lon; }
    public String getDescription() { return description; }
    public Double getAvgRating() { return avgRating; }
    public long getReviewCount() { return reviewCount; }
}
