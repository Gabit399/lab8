package com.example.lab8.grade.service;

import com.example.lab8.course.entity.Course;
import com.example.lab8.course.service.CourseService;
import com.example.lab8.dto.GradeRequest;
import com.example.lab8.dto.StudentTranscriptDto;
import com.example.lab8.exception.ConflictException;
import com.example.lab8.exception.ResourceNotFoundException;
import com.example.lab8.grade.entity.Grade;
import com.example.lab8.grade.repository.GradeRepository;
import com.example.lab8.student.entity.Student;
import com.example.lab8.student.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private static final String TX = "gradeTransactionManager";

    private final GradeRepository gradeRepository;
    private final StudentService studentService;
    private final CourseService courseService;

    public GradeService(GradeRepository gradeRepository, StudentService studentService, CourseService courseService) {
        this.gradeRepository = gradeRepository;
        this.studentService = studentService;
        this.courseService = courseService;
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public Grade findById(Long id) {
        return gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade with id " + id + " not found"));
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public List<Grade> findByStudent(Long studentId) {
        requireStudent(studentId);
        return gradeRepository.findByStudentIdOrderByGradedAtAsc(studentId);
    }

    /**
     * The grade_db transaction only covers the grades table. The student/course checks
     * are separate reads against student_db and course_db.
     */
    @Transactional(transactionManager = TX)
    public Grade assign(GradeRequest request) {
        requireStudent(request.studentId());
        requireCourse(request.courseId());
        if (gradeRepository.existsByStudentIdAndCourseId(request.studentId(), request.courseId())) {
            throw new ConflictException("Student " + request.studentId() + " already has a grade for course "
                    + request.courseId() + "; use PUT /api/grades/{id} to change it");
        }
        Grade grade = new Grade();
        grade.setStudentId(request.studentId());
        grade.setCourseId(request.courseId());
        applyScore(grade, request.score());
        return gradeRepository.save(grade);
    }

    @Transactional(transactionManager = TX)
    public Grade update(Long id, GradeRequest request) {
        Grade grade = findById(id);
        requireStudent(request.studentId());
        requireCourse(request.courseId());
        boolean pairChanged = !grade.getStudentId().equals(request.studentId())
                || !grade.getCourseId().equals(request.courseId());
        if (pairChanged && gradeRepository.existsByStudentIdAndCourseId(request.studentId(), request.courseId())) {
            throw new ConflictException("Student " + request.studentId() + " already has a grade for course "
                    + request.courseId());
        }
        grade.setStudentId(request.studentId());
        grade.setCourseId(request.courseId());
        applyScore(grade, request.score());
        return gradeRepository.save(grade);
    }

    @Transactional(transactionManager = TX)
    public void delete(Long id) {
        gradeRepository.delete(findById(id));
    }

    /** Aggregates data from all three databases. Each read is its own local transaction. */
    public StudentTranscriptDto getTranscript(Long studentId) {
        Student student = studentService.findById(studentId);                                    // student_db
        List<Grade> grades = gradeRepository.findByStudentIdOrderByGradedAtAsc(studentId);       // grade_db

        Map<Long, Course> courses = courseService.findAllById(                                   // course_db
                        grades.stream().map(Grade::getCourseId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Course::getId, Function.identity()));

        List<StudentTranscriptDto.CourseResult> results = grades.stream()
                .map(g -> {
                    Course c = courses.get(g.getCourseId());
                    // A missing course means the logical reference is broken (no FK across databases).
                    return new StudentTranscriptDto.CourseResult(
                            g.getCourseId(),
                            c != null ? c.getCourseCode() : null,
                            c != null ? c.getCourseName() : "Unknown course (id " + g.getCourseId() + ")",
                            c != null ? c.getCredits() : null,
                            c != null ? c.getInstructor() : null,
                            g.getScore(),
                            g.getLetterGrade(),
                            g.getGradedAt());
                })
                .toList();

        int totalCredits = results.stream()
                .filter(r -> r.credits() != null)
                .mapToInt(StudentTranscriptDto.CourseResult::credits)
                .sum();

        Double average = grades.isEmpty() ? null
                : round2(grades.stream().mapToDouble(Grade::getScore).average().orElse(0));

        return new StudentTranscriptDto(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                student.getEmail(),
                student.getMajor(),
                student.getEnrollmentYear(),
                results,
                totalCredits,
                average,
                average == null ? null : toLetterGrade(average));
    }

    public static String toLetterGrade(double score) {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    private void applyScore(Grade grade, Double score) {
        // Defensive check in addition to @Valid on the controller
        if (score == null || score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be between 0 and 100");
        }
        grade.setScore(score);
        grade.setLetterGrade(toLetterGrade(score));
        grade.setGradedAt(LocalDateTime.now());
    }

    private void requireStudent(Long studentId) {
        if (!studentService.exists(studentId)) {
            throw new ResourceNotFoundException("Student with id " + studentId + " not found");
        }
    }

    private void requireCourse(Long courseId) {
        if (!courseService.exists(courseId)) {
            throw new ResourceNotFoundException("Course with id " + courseId + " not found");
        }
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
