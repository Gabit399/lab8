package com.example.lab8.grade.controller;

import com.example.lab8.dto.GradeRequest;
import com.example.lab8.grade.entity.Grade;
import com.example.lab8.grade.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    public List<Grade> getAll() {
        return gradeService.findAll();
    }

    @GetMapping("/{id}")
    public Grade getById(@PathVariable Long id) {
        return gradeService.findById(id);
    }

    @GetMapping("/student/{id}")
    public List<Grade> getByStudent(@PathVariable Long id) {
        return gradeService.findByStudent(id);
    }

    @PostMapping
    public ResponseEntity<Grade> assign(@Valid @RequestBody GradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.assign(request));
    }

    @PutMapping("/{id}")
    public Grade update(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return gradeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        gradeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
