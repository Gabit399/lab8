package com.example.lab8.course.repository;

import com.example.lab8.course.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

/** Bound to course_db via CourseDatabaseConfig. */
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCaseAndIdNot(String courseCode, Long id);
}
