package com.example.walking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class RankingDto {

    private String rankType;
    private LocalDateTime refreshedAt;
    private List<Item> items;

    public RankingDto(String rankType, LocalDateTime refreshedAt, List<Item> items) {
        this.rankType    = rankType;
        this.refreshedAt = refreshedAt;
        this.items       = items;
    }

    public String getRankType()            { return rankType; }
    public LocalDateTime getRefreshedAt()  { return refreshedAt; }
    public List<Item> getItems()           { return items; }

    public static class Item {
        private int rankNo;
        private String courseId;
        private String courseName;
        private BigDecimal score;

        public Item(int rankNo, String courseId, String courseName, BigDecimal score) {
            this.rankNo     = rankNo;
            this.courseId   = courseId;
            this.courseName = courseName;
            this.score      = score;
        }

        public int getRankNo()          { return rankNo; }
        public String getCourseId()     { return courseId; }
        public String getCourseName()   { return courseName; }
        public BigDecimal getScore()    { return score; }
    }
}
