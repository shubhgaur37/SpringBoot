package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.subject.SubjectResponseDTO;
import com.Module3.Practice.CollegeManagement.entity.Subject;
import com.Module3.Practice.CollegeManagement.repository.ProfessorRepository;
import com.Module3.Practice.CollegeManagement.repository.StudentRepository;
import com.Module3.Practice.CollegeManagement.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    /* * ARCHITECTURAL NOTE: CIRCULAR DEPENDENCIES & COUPLING
     * We inject ProfessorRepository directly here rather than ProfessorService.
     * 1. PREVENTS CIRCULAR DEPENDENCIES: Prevents BeanCurrentlyInCreationException if
     * ProfessorService were to need SubjectService.
     * 2. REDUCES TIGHT COUPLING: SubjectService avoids unnecessary dependency on
     * ProfessorService's internal business logic.
     * 3. EFFICIENCY: Direct repository access avoids extra DTO mapping and service-layer overhead.
     */
    private final ProfessorRepository professorRepository;

    private final Logger logger = LoggerFactory.getLogger(SubjectService.class);

    public List<SubjectResponseDTO> getAllSubjects() {
        // Entry tracking (DEBUG)
        logger.debug("Request received to fetch all subjects");

        List<SubjectResponseDTO> subjects = subjectRepository.findAll()
                .stream()
                .map(s -> modelMapper.map(s, SubjectResponseDTO.class))
                .toList();

        // Operational Tracking (INFO): Records total count retrieved for performance monitoring
        logger.info("Successfully retrieved list of all subjects. Total count: {}", subjects.size());

        // Payload State Tracking (TRACE): Detailed item trace for debugging flat DTO structures
        logger.trace("Full subject list payload: {}", subjects);

        return subjects;
    }


    public Optional<SubjectResponseDTO> getSubjectById(Long subjectId) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to fetch Subject ID: {}", subjectId);

        return subjectRepository.findById(subjectId)
                .map(s -> {
                    SubjectResponseDTO responseDTO = modelMapper.map(s, SubjectResponseDTO.class);

                    // Payload State Tracking (TRACE): Safe to log the flat DTO payload
                    logger.trace("Retrieved Subject details for ID {}: {}", subjectId, responseDTO);

                    return responseDTO;
                });
    }

    @Transactional
    public Optional<SubjectResponseDTO> assignSubjectToProfessor(Long subjectId, Long professorId) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to assign Subject ID: {} to Professor ID: {}", subjectId, professorId);

        /*
         * FLATMAP vs MAP:
         * We use flatMap on professorRepository.findById because the lambda function inside
         * it returns an Optional (from subjectRepository.findById). flatMap "unwraps"
         * the inner Optional so we don't end up with Optional<Optional<SubjectResponseDTO>>.
         */
        return professorRepository.findById(professorId).flatMap(newProfessor ->
                subjectRepository.findById(subjectId).map(subject -> {

                    /* * ARCHITECTURAL PIVOT: ORPHAN REMOVAL AGGRESSIVENESS
                     * Previously, we manually removed the subject from the 'old' professor's collection.
                     * However, because 'orphanRemoval = true' is set on the Professor entity,
                     * Hibernate interpreted that removal as a "hard delete" command.
                     * * FLAKINESS FIXED: To prevent the subject from being deleted during reassignment,
                     * we now only update the 'Owner' side (Subject) and sync the 'New' professor.
                     * Hibernate will automatically handle the foreign key update (UPDATE subject SET professor_id=...)
                     * without triggering the orphan removal of the record.
                     */

                    // Re-assigning the owner (This updates the 'professor_id' Foreign Key in DB)
                    subject.setProfessor(newProfessor);

                    // Syncing the new professor's collection (Updates the object graph in memory)
                    newProfessor.getSubjects().add(subject);

                    Subject savedSubject = subjectRepository.save(subject);

                    // Milestone Audit (INFO): Tracks critical data ownership changes
                    logger.info("Successfully reassigned Subject ID: {} to new Professor ID: {}", subjectId, professorId);

                    SubjectResponseDTO responseDTO = modelMapper.map(savedSubject, SubjectResponseDTO.class);

                    // Payload State Tracking (TRACE): Safe to log flat DTO content
                    logger.trace("Updated Subject assignment details: {}", responseDTO);

                    return responseDTO;
                })
        );
    }


    @Transactional
    public Optional<SubjectResponseDTO> assignSubjectToStudent(Long subjectId, Long studentId) {
        // Entry tracking (DEBUG)
        logger.debug("Request received to assign Subject ID: {} to Student ID: {}", subjectId, studentId);

        return studentRepository.findById(studentId).flatMap(student ->
                subjectRepository.findById(subjectId).map(subject -> {

                    /* * MANY-TO-MANY SYNC:
                     * 1. OWNING SIDE: Adding the student to the subject triggers the
                     * insertion into the join table.
                     * 2. INVERSE SIDE: Adding the subject to the student ensures that
                     * if the student object is reused in this transaction, it reflects
                     * the updated roster immediately.
                     */
                    subject.getStudents().add(student);
                    student.getSubjects().add(subject);

                    // Milestone Audit (INFO)
                    logger.info("Successfully associated Student ID: {} with Subject ID: {}", studentId, subjectId);

                    /* * RICH DTO MAPPING:
                     * Because SubjectResponseDTO now contains StudentSummaryDTOs, ModelMapper
                     * will automatically convert the newly updated student Set into
                     * summaries for the response.
                     */
                    SubjectResponseDTO responseDTO = modelMapper.map(subjectRepository.save(subject), SubjectResponseDTO.class);

                    // Payload State Tracking (TRACE)
                    logger.trace("Updated Subject state after mapping: {}", responseDTO);

                    return responseDTO;
                })
        );
    }
}