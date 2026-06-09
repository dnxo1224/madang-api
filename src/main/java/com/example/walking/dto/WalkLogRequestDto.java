package com.example.walking.dto;

import java.time.LocalDate;

/** 산책 기록 추가 요청 (POST /api/walk-logs) */
public class WalkLogRequestDto {
    private Integer userId;
    private String courseId;
    private LocalDate walkedOn;
    private Integer spentMin;   // 선택

    public WalkLogRequestDto() {
    }

    public Integer getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public LocalDate getWalkedOn() { return walkedOn; }
    public Integer getSpentMin() { return spentMin; }

    public void setUserId(Integer userId) { this.userId = userId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setWalkedOn(LocalDate walkedOn) { this.walkedOn = walkedOn; }
    public void setSpentMin(Integer spentMin) { this.spentMin = spentMin; }
}
