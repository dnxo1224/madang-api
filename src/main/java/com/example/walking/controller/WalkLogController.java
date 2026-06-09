package com.example.walking.controller;

import com.example.walking.dto.WalkLogRequestDto;
import com.example.walking.dto.WalkStatsDto;
import com.example.walking.service.WalkLogService;
import com.example.walking.service.WalkStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WalkLogController {

    private final WalkLogService walkLogService;
    private final WalkStatsService walkStatsService;

    public WalkLogController(WalkLogService walkLogService, WalkStatsService walkStatsService) {
        this.walkLogService = walkLogService;
        this.walkStatsService = walkStatsService;
    }

    /** 산책 기록 추가 */
    @PostMapping("/api/walk-logs")
    public ResponseEntity<Void> add(@RequestBody WalkLogRequestDto req) {
        walkLogService.add(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 나의 산책 통계 (인증 미구현 → userId 파라미터) */
    @GetMapping("/api/users/me/stats")
    public WalkStatsDto myStats(@RequestParam int userId) {
        return walkStatsService.getMyStats(userId);
    }
}
