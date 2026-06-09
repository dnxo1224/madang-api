package com.example.walking.service;

import com.example.walking.dto.CourseDetailDto;
import com.example.walking.dto.CourseSearchDto;
import com.example.walking.dto.CourseSummaryDto;
import com.example.walking.dto.NearbyCourseDto;
import com.example.walking.exception.BadRequestException;
import com.example.walking.exception.NotFoundException;
import com.example.walking.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepo;

    public CourseService(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    public List<CourseSummaryDto> search(CourseSearchDto criteria) {
        if (criteria.getMaxLevel() != null
                && (criteria.getMaxLevel() < 1 || criteria.getMaxLevel() > 5)) {
            throw new BadRequestException("maxLevel 은 1~5 사이여야 합니다.");
        }
        return courseRepo.search(criteria);
    }

    public List<NearbyCourseDto> findNearby(double lat, double lon, int limit) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new BadRequestException("좌표 범위가 올바르지 않습니다.");
        }
        if (limit < 1 || limit > 50) {
            throw new BadRequestException("limit 은 1~50 사이여야 합니다.");
        }
        return courseRepo.findNearby(lat, lon, limit);
    }

    public CourseDetailDto getDetail(String courseId) {
        return courseRepo.findDetail(courseId)
                .orElseThrow(() -> new NotFoundException("코스를 찾을 수 없습니다: " + courseId));
    }
}
