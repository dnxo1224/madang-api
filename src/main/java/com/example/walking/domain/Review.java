package com.example.walking.domain;

import java.time.LocalDateTime;

/** review 테이블 — 코스 후기·별점 (코스당 1인 1리뷰) */
public class Review {
    private int reviewId;
    private int userId;
    private String courseId;
    private int rating;       // 1~5
    private String comment;
    private LocalDateTime createdAt;

    public Review() {
    }

    public int getReviewId() { return reviewId; }
    public int getUserId() { return userId; }
    public String getCourseId() { return courseId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setReviewId(int reviewId) { this.reviewId = reviewId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
