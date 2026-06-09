package com.example.walking.scheduler;

import com.example.walking.service.RankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankingScheduler {

    private static final Logger log = LoggerFactory.getLogger(RankingScheduler.class);

    private final RankingService rankingService;

    public RankingScheduler(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /** 매일 오전 9시 랭킹 갱신 */
    @Scheduled(cron = "0 0 9 * * *")
    public void refreshRanking() {
        log.info("랭킹 갱신 시작");
        rankingService.refresh();
        log.info("랭킹 갱신 완료");
    }
}
