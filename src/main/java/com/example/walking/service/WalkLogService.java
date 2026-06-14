package com.example.walking.service;

import com.example.walking.dto.WalkLogDto;
import com.example.walking.dto.WalkLogRequestDto;
import com.example.walking.exception.BadRequestException;
import com.example.walking.exception.NotFoundException;
import com.example.walking.repository.CourseRepository;
import com.example.walking.repository.WalkLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WalkLogService {

    private final WalkLogRepository walkLogRepo;
    private final CourseRepository courseRepo;

    public WalkLogService(WalkLogRepository walkLogRepo, CourseRepository courseRepo) {
        this.walkLogRepo = walkLogRepo;
        this.courseRepo = courseRepo;
    }

    public void add(WalkLogRequestDto req) {
        if (req.getUserId() == null) {
            throw new BadRequestException("userId 는 필수입니다.");
        }
        if (req.getCourseId() == null || req.getCourseId().isBlank()) {
            throw new BadRequestException("courseId 는 필수입니다.");
        }
        if (req.getSpentMin() != null && req.getSpentMin() < 0) {
            throw new BadRequestException("spentMin 은 음수일 수 없습니다.");
        }
        if (!courseRepo.existsById(req.getCourseId())) {
            throw new NotFoundException("코스를 찾을 수 없습니다: " + req.getCourseId());
        }
        LocalDate walkedOn = req.getWalkedOn() != null ? req.getWalkedOn() : LocalDate.now();
        walkLogRepo.insert(req.getUserId(), req.getCourseId(), walkedOn, req.getSpentMin());
    }

    /** 나의 산책 로그 최신순 */
    public List<WalkLogDto> getRecentLogs(int userId, int limit) {
        if (limit < 1 || limit > 100) limit = 20;
        return walkLogRepo.findRecent(userId, limit);
    }
}
