package com.example.walking.service;

import com.example.walking.dto.WalkStatsDto;
import com.example.walking.repository.WalkLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 나의 산책 통계 — 명세서 §5.4 (프로시저 대신 앱 계층에서 집계) */
@Service
public class WalkStatsService {

    private final WalkLogRepository walkLogRepo;

    public WalkStatsService(WalkLogRepository walkLogRepo) {
        this.walkLogRepo = walkLogRepo;
    }

    public WalkStatsDto getMyStats(int userId) {
        Map<String, Object> summary = walkLogRepo.findSummary(userId);
        long walkCount = ((Number) summary.get("walk_count")).longValue();
        BigDecimal totalDistance = WalkLogRepository.asBigDecimal(summary.get("total_distance_km"));
        long totalMinutes = ((Number) summary.get("total_minutes")).longValue();

        List<WalkStatsDto.TopCourse> top3 = walkLogRepo.findTopCourses(userId, 3);
        return new WalkStatsDto(walkCount, totalDistance, totalMinutes, top3);
    }
}
