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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(ProfessorService.class);


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
        logger.debug("Attempting to create professor");

        Professor professor = modelMapper.map(professorDto, Professor.class);
        // Linkage guaranteed safe by DTO validation.
        // This sets the mandatory 'professor_id' on the Subject side.
        professor.getSubjects().forEach(subject -> subject.setProfessor(professor));
        Professor savedProfessor = professorRepository.save(professor);
        ProfessorResponseDTO responseDTO = modelMapper.map(savedProfessor, ProfessorResponseDTO.class);
        logger.trace("Saved Professor Information: {}", responseDTO);

        return responseDTO;
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
            logger.warn("Deletion Terminated: Professor id: {} not found", professorId);
            return;
        }
        logger.debug("Deletion Started for Professor id: {}", professorId);
        professorRepository.deleteById(professorId);
        logger.info("Deletion Complete for Professor id: {}", professorId);
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
        // Entry tracking (DEBUG)
        logger.debug("Request received to remove Subject ID: {} from Professor ID: {}", subjectId, professorId);

        return professorRepository.findById(professorId).flatMap(professor ->
                subjectRepository.findById(subjectId).filter(subject -> {
                    boolean isAssociated = professor.getSubjects().remove(subject);
                    if (!isAssociated) {
                        // Guard Rail (WARN): The subject exists, but doesn't belong to this professor
                        logger.warn("Mismatched Relationship: Subject ID {} is not assigned to Professor ID {}", subjectId, professorId);
                    }
                    return isAssociated;
                }).map(subject -> {

                    // Physical/Context removal for immediate consistency
                    subjectRepository.delete(subject);

                    // Milestone Audit (INFO): Permanent data modification tracking
                    logger.info("Successfully disassociated and deleted Subject ID: {} from Professor ID: {}", subjectId, professorId);

                    ProfessorResponseDTO responseDTO = modelMapper.map(professor, ProfessorResponseDTO.class);

                    // Payload State Tracking (TRACE): Safe to log the flat DTO payload
                    logger.trace("Updated Professor state after subject removal: {}", responseDTO);

                    // Return the updated parent state
                    return responseDTO;
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
        // Entry tracking (DEBUG)
        logger.debug("Request received to assign new subjects to Professor ID: {}. Total requested: {}", professorId, subjectDtos.size());

        return professorRepository.findById(professorId).map(professor -> {

            List<String> requestedTitles = subjectDtos.stream()
                    .map(SubjectRequestDTO::getTitle)
                    .toList();

            List<String> existingTitles = subjectRepository.findExistingTitles(requestedTitles);

            // Internal logic validation tracking (TRACE)
            logger.trace("Filter analysis for Professor ID: {}. Requested: {}, Existing/Skipped: {}",
                    professorId, requestedTitles, existingTitles);

            List<Subject> brandNewSubjects = subjectDtos.stream()
                    .filter(dto -> !existingTitles.contains(dto.getTitle()))
                    .map(dto -> {
                        Subject subject = modelMapper.map(dto, Subject.class);
                        subject.setProfessor(professor);
                        return subject;
                    })
                    .toList();

            if (brandNewSubjects.isEmpty()) {
                // Guard Rail / Operational Tracking (WARN or INFO depending on business criticalness)
                logger.warn("No new subjects were assigned to Professor ID: {}. All requested titles already exist.", professorId);
            } else {
                List<String> assignedTitles = brandNewSubjects.stream().map(Subject::getTitle).toList();
                // Milestone Audit (INFO)
                logger.info("Successfully assigned {} new subjects to Professor ID: {}. Titles: {}",
                        brandNewSubjects.size(), professorId, assignedTitles);
            }

            // Set.addAll ignores duplicates if the user sends the same title twice
            professor.getSubjects().addAll(brandNewSubjects);

            Professor updated = professorRepository.save(professor);
            ProfessorResponseDTO responseDTO = modelMapper.map(updated, ProfessorResponseDTO.class);

            // Payload State Tracking (TRACE)
            logger.trace("Updated Professor state after subject assignment: {}", responseDTO);

            return responseDTO;
        });
    }


    public Optional<ProfessorResponseDTO> getProfessorById(Long id) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to fetch Professor ID: {}", id);

        return professorRepository.findById(id)
                .map(p -> {
                    ProfessorResponseDTO responseDTO = modelMapper.map(p, ProfessorResponseDTO.class);

                    // Payload State Tracking (TRACE): Safe to log the flat DTO payload
                    logger.trace("Retrieved Professor details for ID {}: {}", id, responseDTO);

                    return responseDTO;
                });
    }

    public List<ProfessorResponseDTO> getAllProfessors() {
        // Entry tracking (DEBUG)
        logger.debug("Request received to fetch all professors");

        List<ProfessorResponseDTO> professors = professorRepository.findAll()
                .stream()
                .map(professor -> modelMapper.map(professor, ProfessorResponseDTO.class))
                .toList();

        // Operational Tracking (INFO): Records total count retrieved for monitoring performance
        logger.info("Successfully retrieved list of all professors. Total count: {}", professors.size());

        // Payload State Tracking (TRACE): Detailed item trace for debugging
        logger.trace("Full professor list payload: {}", professors);

        return professors;
    }
}

