package com.example.lab8.grade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Stored in grade_db.grades.
 * studentId and courseId are logical references to other databases, not foreign keys,
 * so they are plain columns instead of @ManyToOne relations.
 */
@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(name = "uk_grades_student_course",
                columnNames = {"student_id", "course_id"}))
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "letter_grade", nullable = false, length = 2)
    private String letterGrade;

    @Column(name = "graded_at", nullable = false)
    private LocalDateTime gradedAt;

    public Grade() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
}
