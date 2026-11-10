package com.example.lab8.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Result of GET /api/students/{id}/transcript, built from student_db, course_db and grade_db. */
public record StudentTranscriptDto(
        Long studentId,
        String studentName,
        String email,
        String major,
        Integer enrollmentYear,
        List<CourseResult> courses,
        int totalCredits,
        Double averageScore,
        String averageLetterGrade
) {

    public record CourseResult(
            Long courseId,
            String courseCode,
            String courseName,
            Integer credits,
            String instructor,
            Double score,
            String letterGrade,
            LocalDateTime gradedAt
    ) {
    }
}
