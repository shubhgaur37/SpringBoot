package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.subject.SubjectResponseDTO;
import com.Module3.Practice.CollegeManagement.repository.ProfessorRepository;
import com.Module3.Practice.CollegeManagement.repository.StudentRepository;
import com.Module3.Practice.CollegeManagement.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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

    public List<SubjectResponseDTO> getAllSubjects() {
        return subjectRepository.findAll()
                .stream()
                .map(s -> modelMapper.map(s, SubjectResponseDTO.class))
                .toList();
    }

    public Optional<SubjectResponseDTO> getSubjectById(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .map(s -> modelMapper.map(s, SubjectResponseDTO.class));
    }

    @Transactional
    public Optional<SubjectResponseDTO> assignSubjectToProfessor(Long subjectId, Long professorId) {
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

                    return modelMapper.map(subjectRepository.save(subject), SubjectResponseDTO.class);
                })
        );
    }

    @Transactional
    public Optional<SubjectResponseDTO> assignSubjectToStudent(Long subjectId, Long studentId) {
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

                    /* * RICH DTO MAPPING:
                     * Because SubjectResponseDTO now contains StudentSummaryDTOs, ModelMapper
                     * will automatically convert the newly updated student Set into
                     * summaries for the response.
                     */
                    return modelMapper.map(subjectRepository.save(subject), SubjectResponseDTO.class);
                })
        );
    }
}