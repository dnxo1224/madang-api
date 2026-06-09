package com.example.walking.domain;

import java.time.LocalDateTime;

/** app_user 테이블 — 서비스 가입 사용자 */
public class AppUser {
    private int userId;
    private String loginId;
    private String nickname;
    private LocalDateTime createdAt;

    public AppUser() {
    }

    public int getUserId() { return userId; }
    public String getLoginId() { return loginId; }
    public String getNickname() { return nickname; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
