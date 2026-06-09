package com.example.walking.dto;

/** 후기 작성 요청 (POST /api/courses/{id}/reviews) */
public class ReviewRequestDto {
    private Integer userId;
    private Integer rating;   // 1~5
    private String comment;

    public ReviewRequestDto() {
    }

    public Integer getUserId() { return userId; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }

    public void setUserId(Integer userId) { this.userId = userId; }
    public void setRating(Integer rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
}
