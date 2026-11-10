package com.example.lab8.student.service;

import com.example.lab8.exception.ConflictException;
import com.example.lab8.exception.ResourceNotFoundException;
import com.example.lab8.grade.repository.GradeRepository;
import com.example.lab8.student.entity.Student;
import com.example.lab8.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private static final String TX = "studentTransactionManager";

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;

    public StudentService(StudentRepository studentRepository, GradeRepository gradeRepository) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
    }

    @Transactional(transactionManager = TX, readOnly = true)
    public boolean exists(Long id) {
        return studentRepository.existsById(id);
    }

    @Transactional(transactionManager = TX)
    public Student create(Student student) {
        if (studentRepository.existsByEmailIgnoreCase(student.getEmail())) {
            throw new ConflictException("Student with email " + student.getEmail() + " already exists");
        }
        student.setId(null);
        return studentRepository.save(student);
    }

    @Transactional(transactionManager = TX)
    public Student update(Long id, Student data) {
        Student existing = findById(id);
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(data.getEmail(), id)) {
            throw new ConflictException("Student with email " + data.getEmail() + " already exists");
        }
        existing.setFirstName(data.getFirstName());
        existing.setLastName(data.getLastName());
        existing.setEmail(data.getEmail());
        existing.setMajor(data.getMajor());
        existing.setEnrollmentYear(data.getEnrollmentYear());
        return studentRepository.save(existing);
    }

    @Transactional(transactionManager = TX)
    public void delete(Long id) {
        Student existing = findById(id);
        // grade_db holds logical references to this student; there is no FK to protect them,
        // so the service refuses to leave orphaned grades behind.
        if (gradeRepository.existsByStudentId(id)) {
            throw new ConflictException("Student with id " + id + " has grades and cannot be deleted");
        }
        studentRepository.delete(existing);
    }
}
