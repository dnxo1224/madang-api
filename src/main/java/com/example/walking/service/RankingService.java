package com.example.walking.service;

import com.example.walking.dto.RankingDto;
import com.example.walking.repository.RankingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RankingService {

    private final RankingRepository rankingRepo;
    private final JdbcTemplate jdbc;

    public RankingService(RankingRepository rankingRepo, JdbcTemplate jdbc) {
        this.rankingRepo = rankingRepo;
        this.jdbc        = jdbc;
    }

    /** sp_refresh_ranking 프로시저를 호출해 course_ranking 테이블을 갱신한다. */
    @Transactional
    public void refresh() {
        jdbc.execute("CALL sp_refresh_ranking()");
    }

    public RankingDto getActivityRanking() {
        return build("ACTIVITY");
    }

    public RankingDto getRatingRanking() {
        return build("RATING");
    }

    private RankingDto build(String rankType) {
        List<RankingDto.Item> items = rankingRepo.findItems(rankType);
        LocalDateTime refreshedAt  = rankingRepo.findRefreshedAt(rankType);
        return new RankingDto(rankType, refreshedAt, items);
    }
}
