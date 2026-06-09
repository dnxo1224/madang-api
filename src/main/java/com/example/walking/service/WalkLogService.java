package com.example.walking.service;

import com.example.walking.dto.WalkLogRequestDto;
import com.example.walking.exception.BadRequestException;
import com.example.walking.exception.NotFoundException;
import com.example.walking.repository.CourseRepository;
import com.example.walking.repository.WalkLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
}
