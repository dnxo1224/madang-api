package com.example.walking.domain;

import java.time.LocalDate;

/** walk_log 테이블 — 사용자 산책 실적 기록 */
public class WalkLog {
    private int logId;
    private int userId;
    private String courseId;
    private LocalDate walkedOn;
    private Integer spentMin;   // NULL 허용

    public WalkLog() {
    }

    public int getLogId() { return logId; }
    public int getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public LocalDate getWalkedOn() { return walkedOn; }
    public Integer getSpentMin() { return spentMin; }

    public void setLogId(int logId) { this.logId = logId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setWalkedOn(LocalDate walkedOn) { this.walkedOn = walkedOn; }
    public void setSpentMin(Integer spentMin) { this.spentMin = spentMin; }
}
