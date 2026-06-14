package com.example.walking.controller;

import com.example.walking.dto.RankingDto;
import com.example.walking.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /** 즐겨찾기 + 리뷰 수 합산 TOP 5 */
    @GetMapping("/activity")
    public RankingDto activityRanking() {
        return rankingService.getActivityRanking();
    }

    /** 평균 평점 TOP 5 */
    @GetMapping("/rating")
    public RankingDto ratingRanking() {
        return rankingService.getRatingRanking();
    }

    /** 랭킹 즉시 갱신 — sp_refresh_ranking 프로시저 호출 (데모/수동 트리거용). */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh() {
        rankingService.refresh();
        return ResponseEntity.ok(Map.of("status", "refreshed"));
    }
}
