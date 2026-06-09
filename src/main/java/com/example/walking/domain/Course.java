package com.example.walking.domain;

import java.math.BigDecimal;

/** course 테이블 한 행 (원천 정제 데이터) */
public class Course {
    private String courseId;
    private String courseName;
    private String flagName;
    private int regionId;
    private int levelId;
    private BigDecimal lengthKm;   // NULL 허용
    private Integer timeMin;       // NULL 허용
    private String toiletInfo;
    private String storeInfo;
    private BigDecimal lat;
    private BigDecimal lon;
    private String description;

    public Course() {
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getFlagName() { return flagName; }
    public int getRegionId() { return regionId; }
    public int getLevelId() { return levelId; }
    public BigDecimal getLengthKm() { return lengthKm; }
    public Integer getTimeMin() { return timeMin; }
    public String getToiletInfo() { return toiletInfo; }
    public String getStoreInfo() { return storeInfo; }
    public BigDecimal getLat() { return lat; }
    public BigDecimal getLon() { return lon; }
    public String getDescription() { return description; }

    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setFlagName(String flagName) { this.flagName = flagName; }
    public void setRegionId(int regionId) { this.regionId = regionId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }
    public void setLengthKm(BigDecimal lengthKm) { this.lengthKm = lengthKm; }
    public void setTimeMin(Integer timeMin) { this.timeMin = timeMin; }
    public void setToiletInfo(String toiletInfo) { this.toiletInfo = toiletInfo; }
    public void setStoreInfo(String storeInfo) { this.storeInfo = storeInfo; }
    public void setLat(BigDecimal lat) { this.lat = lat; }
    public void setLon(BigDecimal lon) { this.lon = lon; }
    public void setDescription(String description) { this.description = description; }
}
