package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorResponseDTO;
import com.Module3.Practice.CollegeManagement.dto.subject.SubjectRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentResponseDTO;
import com.Module3.Practice.CollegeManagement.entity.Professor;
import com.Module3.Practice.CollegeManagement.entity.Subject;
import com.Module3.Practice.CollegeManagement.entity.Student;
import com.Module3.Practice.CollegeManagement.repository.ProfessorRepository;
import com.Module3.Practice.CollegeManagement.repository.SubjectRepository;
import com.Module3.Practice.CollegeManagement.repository.StudentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProfessorServiceIntegrationTests {

    @Autowired private ProfessorService professorService;
    @Autowired private StudentService studentService;
    @Autowired private SubjectService subjectService;

    @Autowired private ProfessorRepository professorRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private StudentRepository studentRepository;

    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("FLOW: Create Professor - Should cascade PERSIST to subjects")
    void testCreateProfessorCascade() {
        ProfessorRequestDTO request = getProfessorRequest("Minerva McGonagall", "Transfiguration");

        ProfessorResponseDTO response = professorService.createProfessor(request);
        flushAndClear();

        assertThat(response.getSubjects()).hasSize(1);
        assertThat(subjectRepository.findAll()).extracting(Subject::getTitle).contains("Transfiguration");
    }

    @Test
    @DisplayName("FLOW: Remove Subject - Consistency between L1 Cache and returned DTO")
    void testRemoveSubjectPersistenceConsistency() {
        // 1. Arrange
        ProfessorResponseDTO saved = professorService.createProfessor(getProfessorRequest("Remus Lupin", "DADA"));
        Long subjectId = saved.getSubjects().iterator().next().getId();

        // Find the entity to track its state in the Persistence Context
        Subject subjectEntity = entityManager.find(Subject.class, subjectId);
        assertThat(entityManager.contains(subjectEntity)).isTrue();

        // 2. Act
        Optional<ProfessorResponseDTO> result = professorService.removeSubject(saved.getId(), subjectId);

        // 3. Assert: Persistence Context Consistency
        assertThat(result).isPresent();

        /* * We verify that the subject is no longer 'Managed' by Hibernate.
         * This proves our service successfully transitioned the entity to
         * the REMOVED state immediately via subjectRepository.delete().
         */
        assertThat(entityManager.contains(subjectEntity))
                .as("Subject should be evicted from L1 Cache immediately for consistency")
                .isFalse();

        // verify the DTO returned to the controller is updated
        assertThat(result.get().getSubjects()).isEmpty();
    }

    @Test
    @DisplayName("FLOW: Remove Subject - Should return empty Optional if IDs are invalid")
    void testRemoveSubjectInvalidFlow() {
        // Act: Try to remove a subject from a non-existent professor
        Optional<ProfessorResponseDTO> result = professorService.removeSubject(999L, 1L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("FLOW: Deleting Professor - Should cleanup Subjects and Student links")
    void testProfessorDeletionCleanup() {
        ProfessorResponseDTO profRes = professorService.createProfessor(getProfessorRequest("Gilderoy Lockhart", "Magical Me"));
        Long subjectId = profRes.getSubjects().iterator().next().getId();

        StudentResponseDTO stuRes = studentService.createStudent(new StudentRequestDTO("Hermione Granger"));
        subjectService.assignSubjectToStudent(subjectId, stuRes.getId());
        flushAndClear();

        // Act
        professorService.deleteProfessor(profRes.getId());

        // DETACH STRATEGY: Prevents stale cache from showing deleted subjects in Student
        Student staleStudent = studentRepository.findById(stuRes.getId()).get();
        entityManager.detach(staleStudent);

        flushAndClear();

        // Assert
        assertThat(professorRepository.existsById(profRes.getId())).isFalse();
        assertThat(subjectRepository.existsById(subjectId)).isFalse();
        assertThat(studentRepository.findById(stuRes.getId()).get().getSubjects()).isEmpty();
    }

    @Test
    @DisplayName("LOGIC: assignNewSubjectsOnly - Should ignore existing titles")
    void testAssignNewSubjectsOnly() {
        ProfessorResponseDTO saved = professorService.createProfessor(getProfessorRequest("Filius Flitwick", "Charms"));

        SubjectRequestDTO duplicate = new SubjectRequestDTO();
        duplicate.setTitle("Charms");
        SubjectRequestDTO brandNew = new SubjectRequestDTO();
        brandNew.setTitle("Duelling");

        professorService.assignNewSubjectsOnly(saved.getId(), List.of(duplicate, brandNew));
        flushAndClear();

        Professor retrieved = professorRepository.findById(saved.getId()).get();
        assertThat(retrieved.getSubjects()).hasSize(2);
        assertThat(retrieved.getSubjects()).extracting(Subject::getTitle)
                .containsExactlyInAnyOrder("Charms", "Duelling");
    }

    // --- PRIVATE HELPERS ---

    private ProfessorRequestDTO getProfessorRequest(String name, String subjectTitle) {
        ProfessorRequestDTO request = new ProfessorRequestDTO();
        request.setName(name);

        SubjectRequestDTO sub = new SubjectRequestDTO();
        sub.setTitle(subjectTitle);
        request.setSubjects(List.of(sub));

        return request;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}