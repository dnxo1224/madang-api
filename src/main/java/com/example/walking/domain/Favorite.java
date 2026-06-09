package com.example.walking.domain;

import java.time.LocalDateTime;

/** favorite 테이블 — 사용자–코스 즐겨찾기 (복합키 user_id+course_id) */
public class Favorite {
    private int userId;
    private String courseId;
    private LocalDateTime addedAt;

    public Favorite() {
    }

    public int getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public LocalDateTime getAddedAt() { return addedAt; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
