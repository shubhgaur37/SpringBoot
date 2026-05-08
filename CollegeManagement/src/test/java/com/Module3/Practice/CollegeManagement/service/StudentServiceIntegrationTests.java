package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.student.StudentRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentResponseDTO;
import com.Module3.Practice.CollegeManagement.entity.Professor;
import com.Module3.Practice.CollegeManagement.entity.Student;
import com.Module3.Practice.CollegeManagement.entity.Subject;
import com.Module3.Practice.CollegeManagement.repository.AdmissionRecordRepository;
import com.Module3.Practice.CollegeManagement.repository.ProfessorRepository;
import com.Module3.Practice.CollegeManagement.repository.StudentRepository;
import com.Module3.Practice.CollegeManagement.repository.SubjectRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ARCHITECTURAL INTEGRATION TEST: STUDENT DOMAIN
 * * CORE STRATEGY:
 * We avoid @PreRemove hooks in entities (performance bottleneck) and
 * manual severing in Services (tight coupling).
 * * TESTING STRATEGY:
 * We use 'Detach & Flush' to manage the Persistence Context. This manually
 * breaks the Java-level link between entities in the L1 cache so that
 * Hibernate can issue DELETE queries without "Transient" reference errors.
 */
@SpringBootTest
@Transactional
class StudentServiceIntegrationTests {

    @Autowired private StudentService studentService;
    @Autowired private SubjectService subjectService;
    @Autowired private StudentRepository studentRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private ProfessorRepository professorRepository;
    @Autowired private AdmissionRecordRepository admissionRecordRepository;

    @Autowired private EntityManager entityManager;

    private Long sharedSubjectId;

    @BeforeEach
    void setUp() {
        Professor prof = new Professor();
        prof.setName("Professor Remus Lupin");
        Professor savedProf = professorRepository.save(prof);

        Subject sub = new Subject();
        sub.setTitle("Defense Against the Dark Arts");
        sub.setProfessor(savedProf);
        sharedSubjectId = subjectRepository.save(sub).getId();
    }

    @Test
    @DisplayName("INTEGRATION: Deleting Student - Using Detach to overcome stale L1 Cache")
    void testStudentDeletionAndCascading() {
        // 1. Arrange
        StudentResponseDTO savedStudent = studentService.createStudent(new StudentRequestDTO("Luna Lovegood"));
        Long studentId = savedStudent.getId();
        subjectService.assignSubjectToStudent(sharedSubjectId, studentId);

        Long admissionId = studentRepository.findById(studentId).get().getAdmissionRecord().getId();

        // 2. ACT: Delete Student
        studentService.deleteStudent(studentId);

        /* * --- THE DETACH STRATEGY ---
         * PROBLEM: The 'Subject' object in the L1 Cache still holds a reference to the deleted Student.
         * If we flush now, Hibernate throws TransientPropertyValueException.
         * * SOLUTION:
         * 1. Get the 'Subject' that is holding the stale reference.
         * 2. DETACH: Remove it from the Persistence Context so Hibernate stops tracking it.
         * 3. FLUSH: Hibernate can now delete the Student row safely.
         */
        Subject staleSubject = subjectRepository.findById(sharedSubjectId).get();
        entityManager.detach(staleSubject);

        entityManager.flush(); // Synchronize DELETE SQL to DB
        entityManager.clear(); // Wipe cache to force fresh SELECTs for assertions

        // 3. ASSERT
        assertThat(studentRepository.existsById(studentId)).isFalse();
        assertThat(admissionRecordRepository.existsById(admissionId)).isFalse();

        // Verification: Fresh fetch proves the Join Table was cleaned by the DB
        Subject freshSubject = subjectRepository.findById(sharedSubjectId).get();
        assertThat(freshSubject.getStudents()).isEmpty();
    }

    @Test
    @DisplayName("INTEGRATION: Deleting a Subject - Should clean Join Table and reflect in Student Set")
    void testSubjectDeletionCleanup() {
        StudentResponseDTO student = studentService.createStudent(new StudentRequestDTO("Draco Malfoy"));
        subjectService.assignSubjectToStudent(sharedSubjectId, student.getId());

        // Act
        subjectRepository.deleteById(sharedSubjectId);

        /* * AVOID STALE DATA *
         * The 'Student' object in memory still thinks Draco is in the subject.
         * We flush and clear to force a reload from the Database truth.
         */
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(subjectRepository.existsById(sharedSubjectId)).isFalse();
        Student retrieved = studentRepository.findById(student.getId()).orElseThrow();
        assertThat(retrieved.getSubjects()).isEmpty();
    }

    @Test
    @DisplayName("FUNC: getAllStudents - Should return mapped DTO list")
    void testGetAllStudents() {
        studentService.createStudent(new StudentRequestDTO("Cedric Diggory"));
        studentService.createStudent(new StudentRequestDTO("Cho Chang"));

        List<StudentResponseDTO> results = studentService.getAllStudents();

        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results).extracting(StudentResponseDTO::getName).contains("Cedric Diggory", "Cho Chang");
    }
}