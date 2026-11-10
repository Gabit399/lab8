package com.example.lab8;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class Lab8ApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    @Qualifier("studentDataSource")
    private DataSource studentDataSource;

    @Autowired
    @Qualifier("courseDataSource")
    private DataSource courseDataSource;

    @Autowired
    @Qualifier("gradeDataSource")
    private DataSource gradeDataSource;

    @Test
    void eachRepositoryUsesItsOwnDatabase() throws Exception {
        assertThat(url(studentDataSource)).contains("student_db");
        assertThat(url(courseDataSource)).contains("course_db");
        assertThat(url(gradeDataSource)).contains("grade_db");
        assertThat(hasTable(studentDataSource, "STUDENTS")).isTrue();
        assertThat(hasTable(studentDataSource, "GRADES")).isFalse();
        assertThat(hasTable(courseDataSource, "COURSES")).isTrue();
        assertThat(hasTable(courseDataSource, "STUDENTS")).isFalse();
        assertThat(hasTable(gradeDataSource, "GRADES")).isTrue();
        assertThat(hasTable(gradeDataSource, "COURSES")).isFalse();
    }

    @Test
    void fullFlowAndErrorCases() throws Exception {
        long studentId = createAndGetId("/api/students", """
                {"firstName":"Aruzhan","lastName":"Seitkali","email":"aruzhan@uni.kz",
                 "major":"Computer Science","enrollmentYear":2023}""");

        // duplicate email -> 409
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"X","lastName":"Y","email":"ARUZHAN@uni.kz","enrollmentYear":2023}"""))
                .andExpect(status().isConflict());

        // invalid body -> 400 with field errors
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"","lastName":"Y","email":"not-an-email"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists());

        long webId = createAndGetId("/api/courses", """
                {"courseName":"Web Applications in Java Spring","courseCode":"CSE301","credits":5,"instructor":"Dr. A"}""");
        long dbId = createAndGetId("/api/courses", """
                {"courseName":"Database Systems","courseCode":"CSE220","credits":5,"instructor":"Dr. B"}""");

        // duplicate course code -> 409
        mvc.perform(post("/api/courses").contentType(MediaType.APPLICATION_JSON).content("""
                        {"courseName":"Copy","courseCode":"cse301","credits":3}"""))
                .andExpect(status().isConflict());

        // update course
        mvc.perform(put("/api/courses/" + dbId).contentType(MediaType.APPLICATION_JSON).content("""
                        {"courseName":"Database Systems II","courseCode":"CSE220","credits":6,"instructor":"Dr. B"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credits").value(6));

        // grades
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content(grade(studentId, webId, 95)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.letterGrade").value("A"));
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content(grade(studentId, dbId, 78.5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.letterGrade").value("C"));

        // invalid score -> 400, nonexistent student/course -> 404, duplicate pair -> 409
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content(grade(studentId, webId, 101)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content(grade(999, webId, 50)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content(grade(studentId, 999, 50)))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/grades").contentType(MediaType.APPLICATION_JSON)
                        .content(grade(studentId, webId, 60)))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/grades/student/" + studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/grades/student/999")).andExpect(status().isNotFound());

        // transcript combines all three databases
        mvc.perform(get("/api/students/" + studentId + "/transcript"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Aruzhan Seitkali"))
                .andExpect(jsonPath("$.courses.length()").value(2))
                .andExpect(jsonPath("$.courses[0].courseCode").value("CSE301"))
                .andExpect(jsonPath("$.courses[1].courseName").value("Database Systems II"))
                .andExpect(jsonPath("$.totalCredits").value(11))
                .andExpect(jsonPath("$.averageScore").value(86.75))
                .andExpect(jsonPath("$.averageLetterGrade").value("B"));
        mvc.perform(get("/api/students/999/transcript")).andExpect(status().isNotFound());

        // student/course with grades cannot be deleted -> 409
        mvc.perform(delete("/api/students/" + studentId)).andExpect(status().isConflict());
        mvc.perform(delete("/api/courses/" + webId)).andExpect(status().isConflict());

        // update student, then delete one without grades
        mvc.perform(put("/api/students/" + studentId).contentType(MediaType.APPLICATION_JSON).content("""
                        {"firstName":"Aruzhan","lastName":"Seitkali","email":"aruzhan@uni.kz",
                         "major":"Software Engineering","enrollmentYear":2023}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.major").value("Software Engineering"));
        long other = createAndGetId("/api/students", """
                {"firstName":"Daniyar","lastName":"Bekov","email":"daniyar@uni.kz","enrollmentYear":2022}""");
        mvc.perform(delete("/api/students/" + other)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/students/" + other)).andExpect(status().isNotFound());
        mvc.perform(get("/api/students/abc")).andExpect(status().isBadRequest());
    }

    private long createAndGetId(String path, String json) throws Exception {
        MvcResult result = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(result.getResponse().getContentAsString());
        assertThat(m.find()).isTrue();
        return Long.parseLong(m.group(1));
    }

    private static String grade(long studentId, long courseId, double score) {
        return "{\"studentId\":" + studentId + ",\"courseId\":" + courseId + ",\"score\":" + score + "}";
    }

    private static String url(DataSource ds) throws Exception {
        try (Connection c = ds.getConnection()) {
            return c.getMetaData().getURL();
        }
    }

    private static boolean hasTable(DataSource ds, String table) throws Exception {
        try (Connection c = ds.getConnection();
             var rs = c.getMetaData().getTables(null, null, table, null)) {
            return rs.next();
        }
    }
}
