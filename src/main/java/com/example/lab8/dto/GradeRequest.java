package com.example.lab8.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Body of POST /api/grades and PUT /api/grades/{id}. letter_grade and graded_at are set by the server. */
public record GradeRequest(
        @NotNull(message = "studentId is required")
        Long studentId,

        @NotNull(message = "courseId is required")
        Long courseId,

        @NotNull(message = "score is required")
        @DecimalMin(value = "0.0", message = "score must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "score must be between 0 and 100")
        Double score
) {
}
