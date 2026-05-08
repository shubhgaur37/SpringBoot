package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.professor.ProfessorResponseDTO;
import com.Module3.Practice.CollegeManagement.dto.subject.SubjectRequestDTO;
import com.Module3.Practice.CollegeManagement.entity.Professor;
import com.Module3.Practice.CollegeManagement.entity.Subject;
import com.Module3.Practice.CollegeManagement.repository.ProfessorRepository;
import com.Module3.Practice.CollegeManagement.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Creates a new Professor and their associated Subjects.
 * <p>
 * --- TRADE-OFFS & ARCHITECTURAL DECISIONS ---
 * 1. KISS vs. SRP:
 * Mapping here avoids circular service dependencies.
 * * 2. BI-DIRECTIONAL SYNC (The "Back-Link"):
 * Since 'Subject' is the Owning Side, we MUST call s.setProfessor(professor).
 * * 3. RECURSION GUARD (Model Mapping):
 * Mapping to SummaryDTOs kills the loop during the mapping phase.
 * * --- REMOVAL OF NULL CHECK ---
 * Removed because the "DTO Firewall" (@NotEmpty) guarantees that the
 * collection is populated before the service logic executes.
 */


@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final SubjectRepository subjectRepository;
    private final ModelMapper modelMapper;


    /**
     * Creates a new Professor and cascades persistence to the provided subjects.
     * * * NOTE: This method assumes all provided subjects are brand new.
     * * ERROR HANDLING: Mapping existing subjects is NOT supported. If a subject
     * title in the DTO already exists in the database, this flow will trigger a
     * Unique Constraint Violation (DataIntegrityViolationException) at the
     * database level during the flush/commit phase.
     */
    @Transactional
    public ProfessorResponseDTO createProfessor(ProfessorRequestDTO professorDto) {
        Professor professor = modelMapper.map(professorDto, Professor.class);

        // Linkage guaranteed safe by DTO validation.
        // This sets the mandatory 'professor_id' on the Subject side.
        professor.getSubjects().forEach(subject -> subject.setProfessor(professor));

        Professor savedProfessor = professorRepository.save(professor);
        return modelMapper.map(savedProfessor, ProfessorResponseDTO.class);
    }

    /**
     * Deletes a professor and tests Cascade DELETE.
     * --- CASCADE DELETE ---
     * CascadeType.ALL ensures that deleting the Professor automatically
     * deletes all associated Subject records from the database.
     */
    @Transactional
    public void deleteProfessor(Long professorId) {
        if (!professorRepository.existsById(professorId)) {
            return;
        }
        professorRepository.deleteById(professorId);
    }

    /**
     * Removes a specific subject from a professor and returns the updated state.
     * <p>
     * NOTE: Explicitly calling subjectRepository.delete() ensures Persistence Context
     * consistency by moving the Subject to the REMOVED state immediately.
     * This prevents stale data reads within the same transaction.
     */
    @Transactional
    public Optional<ProfessorResponseDTO> removeSubject(Long professorId, Long subjectId) {
        return professorRepository.findById(professorId).flatMap(professor ->
                subjectRepository.findById(subjectId).filter(subject ->
                        professor.getSubjects().remove(subject)).map(subject -> {

                    // Physical/Context removal for immediate consistency
                    subjectRepository.delete(subject);

                    // Return the updated parent state
                    return modelMapper.map(professor, ProfessorResponseDTO.class);
                })
        );
    }

    /**
     * Assigns ONLY brand-new subjects to a professor.
     * <p>
     * --- OPTIMIZATION & RECURSION LOGIC ---
     * 1. PROJECTED QUERIES: Fetching Strings only to minimize I/O.
     * 2. RECURSION GUARD: SummaryDTO prevents circular JSON paths.
     * 3. AGGREGATE ROOT: All changes flow through the Professor entity.
     */
    @Transactional
    public Optional<ProfessorResponseDTO> assignNewSubjectsOnly(Long professorId, List<SubjectRequestDTO> subjectDtos) {
        return professorRepository.findById(professorId).map(professor -> {

            List<String> requestedTitles = subjectDtos.stream()
                    .map(SubjectRequestDTO::getTitle)
                    .toList();

            List<String> existingTitles = subjectRepository.findExistingTitles(requestedTitles);

            List<Subject> brandNewSubjects = subjectDtos.stream()
                    .filter(dto -> !existingTitles.contains(dto.getTitle()))
                    .map(dto -> {
                        Subject subject = modelMapper.map(dto, Subject.class);
                        subject.setProfessor(professor);
                        return subject;
                    })
                    .toList();

            // Set.addAll ignores duplicates if the user sends the same title twice
            professor.getSubjects().addAll(brandNewSubjects);

            Professor updated = professorRepository.save(professor);
            return modelMapper.map(updated, ProfessorResponseDTO.class);
        });
    }

    public Optional<ProfessorResponseDTO> getProfessorById(Long id) {
        return professorRepository.findById(id)
                .map(p -> modelMapper.map(p, ProfessorResponseDTO.class));
    }

    public List<ProfessorResponseDTO> getAllProfessors() {
        return professorRepository.findAll()
                .stream()
                .map(professor -> modelMapper.map(professor, ProfessorResponseDTO.class))
                .toList();
    }
}

