package com.example.lab8.course.service;

import com.example.lab8.course.entity.Course;
import com.example.lab8.course.repository.CourseRepository;
import com.example.lab8.exception.ConflictException;
import com.example.lab8.exception.ResourceNotFoundException;
import com.example.lab8.grade.repository.GradeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
public class CourseService {

    private static final String TX = "courseTransactionManager";

    private final CourseRepository courseRepository;
    private final GradeRepository gradeRepository;

    public CourseService(CourseRepository courseRepository, GradeRepository gradeRepository) {
        this.courseRepository = courseRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public List<Course> findAllById(Collection<Long> ids) {
        return courseRepository.findAllById(ids);
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course with id " + id + " not found"));
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public boolean exists(Long id) {
        return courseRepository.existsById(id);
    }

    @Transactional(transactionManager = TX)
    public Course create(Course course) {
        if (courseRepository.existsByCourseCodeIgnoreCase(course.getCourseCode())) {
            throw new ConflictException("Course with code " + course.getCourseCode() + " already exists");
        }
        course.setId(null);
        return courseRepository.save(course);
    }

    @Transactional(transactionManager = TX)
    public Course update(Long id, Course data) {
        Course existing = findById(id);
        if (courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(data.getCourseCode(), id)) {
            throw new ConflictException("Course with code " + data.getCourseCode() + " already exists");
        }
        existing.setCourseName(data.getCourseName());
        existing.setCourseCode(data.getCourseCode());
        existing.setCredits(data.getCredits());
        existing.setInstructor(data.getInstructor());
        return courseRepository.save(existing);
    }

    @Transactional(transactionManager = TX)
    public void delete(Long id) {
        Course existing = findById(id);
        if (gradeRepository.existsByCourseId(id)) {
            throw new ConflictException("Course with id " + id + " has grades and cannot be deleted");
        }
        courseRepository.delete(existing);
    }
}
