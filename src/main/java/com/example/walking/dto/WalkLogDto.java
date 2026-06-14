package com.example.walking.dto;

import java.time.LocalDate;

/** 나의 산책 로그 한 행 (GET /api/users/me/walk-logs) */
public class WalkLogDto {
    private long logId;
    private String courseId;
    private String courseName;
    private String sido;
    private String sigungu;
    private LocalDate walkedOn;
    private Integer spentMin;

    public WalkLogDto(long logId, String courseId, String courseName,
                      String sido, String sigungu, LocalDate walkedOn, Integer spentMin) {
        this.logId = logId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.sido = sido;
        this.sigungu = sigungu;
        this.walkedOn = walkedOn;
        this.spentMin = spentMin;
    }

    public long getLogId() { return logId; }
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getSido() { return sido; }
    public String getSigungu() { return sigungu; }
    public LocalDate getWalkedOn() { return walkedOn; }
    public Integer getSpentMin() { return spentMin; }
}
