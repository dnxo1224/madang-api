package com.example.walking.dto;

import java.math.BigDecimal;

/** 조건검색 파라미터 (GET /api/courses). 모든 값 선택적(NULL=조건 미적용) */
public class CourseSearchDto {
    private String sido;          // 시·도 (예: '경기')
    private Integer maxLevel;     // 난이도 이하 (level_id <= maxLevel)
    private BigDecimal maxLength; // 거리 이하 (length_km < maxLength)
    private String sort;          // 'length' | 'time' (그 외/NULL = 이름순)

    public String getSido() { return sido; }
    public Integer getMaxLevel() { return maxLevel; }
    public BigDecimal getMaxLength() { return maxLength; }
    public String getSort() { return sort; }

    public void setSido(String sido) { this.sido = sido; }
    public void setMaxLevel(Integer maxLevel) { this.maxLevel = maxLevel; }
    public void setMaxLength(BigDecimal maxLength) { this.maxLength = maxLength; }
    public void setSort(String sort) { this.sort = sort; }
}
