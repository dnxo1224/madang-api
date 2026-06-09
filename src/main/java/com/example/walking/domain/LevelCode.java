package com.example.walking.domain;

/** level_code 테이블 — 난이도 코드(1~5)와 명칭 */
public class LevelCode {
    private int levelId;
    private String levelName;

    public LevelCode() {
    }

    public int getLevelId() { return levelId; }
    public String getLevelName() { return levelName; }

    public void setLevelId(int levelId) { this.levelId = levelId; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
}
