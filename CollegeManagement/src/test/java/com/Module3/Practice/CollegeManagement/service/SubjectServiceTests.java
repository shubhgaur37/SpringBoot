package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorResponseDTO;
import com.Module3.Practice.CollegeManagement.dto.subject.SubjectRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.subject.SubjectResponseDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentResponseDTO;
import com.Module3.Practice.CollegeManagement.entity.Professor;
import com.Module3.Practice.CollegeManagement.entity.Subject;
import com.Module3.Practice.CollegeManagement.repository.ProfessorRepository;
import com.Module3.Practice.CollegeManagement.repository.SubjectRepository;
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
class SubjectServiceIntegrationTests {

    @Autowired private SubjectService subjectService;
    @Autowired private ProfessorService professorService;
    @Autowired private StudentService studentService;

    @Autowired private ProfessorRepository professorRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private EntityManager entityManager;

    @Test
    @DisplayName("FLOW: Reassign Subject - Should move Subject and sync both Professors' collections")
    void testAssignSubjectToProfessorFlow() {
        // 1. Arrange: Create two professors. Prof A has the subject.
        ProfessorResponseDTO profA = professorService.createProfessor(getProfessorRequest("Severus Snape", "Potions"));
        ProfessorResponseDTO profB = professorService.createProfessor(getProfessorRequest("Horace Slughorn", "Advanced Potions"));

        Long subjectId = profA.getSubjects().iterator().next().getId();

        // Ensure data is in DB and clear cache to start fresh for the 'Act'
        flushAndClear();

        // 2. Act: Reassign 'Potions' from Snape to Slughorn
        Optional<SubjectResponseDTO> result = subjectService.assignSubjectToProfessor(subjectId, profB.getId());

        // 3. Assert: Persistence Context & Logic Consistency
        assertThat(result).isPresent();
        assertThat(result.get().getProfessor().getName()).isEqualTo("Horace Slughorn");

        /* * CRITICAL FIX: REFRESH CACHE *
         * Since we no longer manually remove the subject from the old professor
         * in the service (to avoid Orphan Removal bugs), the L1 cache might
         * still show the old relationship. We flush/clear to force Hibernate
         * to fetch the truth from the DB.
         */
        flushAndClear();

        // Verify the old professor's collection is clean (Database truth)
        Professor snape = professorRepository.findById(profA.getId()).get();
        assertThat(snape.getSubjects()).isEmpty();

        // Verify the new professor's collection is updated
        Professor slughorn = professorRepository.findById(profB.getId()).get();
        assertThat(slughorn.getSubjects()).extracting(Subject::getTitle).contains("Potions");
    }
    @Test
    @DisplayName("FLOW: Assign to Student - Should update Many-to-Many join table links")
    void testAssignSubjectToStudentFlow() {
        // 1. Arrange
        ProfessorResponseDTO prof = professorService.createProfessor(getProfessorRequest("Alastor Moody", "Defense Against the Dark Arts"));
        Long subjectId = prof.getSubjects().iterator().next().getId();
        StudentResponseDTO student = studentService.createStudent(new StudentRequestDTO("Neville Longbottom"));
        flushAndClear();

        // 2. Act
        Optional<SubjectResponseDTO> result = subjectService.assignSubjectToStudent(subjectId, student.getId());

        // 3. Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStudents()).extracting("name").contains("Neville Longbottom");

        // Verify the inverse side (Student) also sees the subject
        // This confirms the in-memory sync 'student.getSubjects().add(subject)' worked
        StudentResponseDTO updatedStudent = studentService.getStudentById(student.getId()).get();
        assertThat(updatedStudent.getSubjects()).extracting("title").contains("Defense Against the Dark Arts");
    }

    @Test
    @DisplayName("FLOW: Subject Retrieval - Should return mapped DTOs with children")
    void testGetSubjectByIdFlow() {
        // Arrange
        ProfessorResponseDTO saved = professorService.createProfessor(getProfessorRequest("Pomona Sprout", "Herbology"));
        Long id = saved.getSubjects().iterator().next().getId();
        flushAndClear();

        // Act
        Optional<SubjectResponseDTO> result = subjectService.getSubjectById(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Herbology");
        assertThat(result.get().getProfessor().getName()).isEqualTo("Pomona Sprout");
    }

    // --- HELPERS ---

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