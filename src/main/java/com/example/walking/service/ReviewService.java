package com.example.walking.service;

import com.example.walking.dto.ReviewRequestDto;
import com.example.walking.exception.BadRequestException;
import com.example.walking.exception.ConflictException;
import com.example.walking.exception.NotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Types;

@Service
public class ReviewService {

    private final JdbcTemplate jdbc;

    public ReviewService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 리뷰 작성 — sp_add_review 프로시저 호출.
     * 프로시저 내부에서 사용자/코스 존재 여부 + 중복 리뷰를 검증하고
     * OUT 파라미터 p_result 로 결과 코드를 반환한다.
     */
    public void create(String courseId, ReviewRequestDto req) {
        if (req.getUserId() == null) {
            throw new BadRequestException("userId 는 필수입니다.");
        }
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5) {
            throw new BadRequestException("rating 은 1~5 사이여야 합니다.");
        }

        String result = jdbc.execute(
                "{call sp_add_review(?, ?, ?, ?, ?)}",
                (CallableStatement cs) -> {
                    cs.setInt(1, req.getUserId());          // IN  p_user_id
                    cs.setString(2, courseId);              // IN  p_course_id
                    cs.setByte(3, req.getRating().byteValue()); // IN  p_rating
                    cs.setString(4, req.getComment());      // IN  p_comment
                    cs.registerOutParameter(5, Types.VARCHAR);  // OUT p_result
                    cs.execute();
                    return cs.getString(5);
                });

        // 프로시저 반환 코드에 따라 예외 발생
        switch (result) {
            case "USER_NOT_FOUND"   -> throw new NotFoundException("사용자를 찾을 수 없습니다.");
            case "COURSE_NOT_FOUND" -> throw new NotFoundException("코스를 찾을 수 없습니다: " + courseId);
            case "ALREADY_REVIEWED" -> throw new ConflictException("이미 이 코스에 작성한 리뷰가 있습니다.");
            // "OK" → 정상, 아무 것도 하지 않음
        }
    }
}
