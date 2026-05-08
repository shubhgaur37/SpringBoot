package com.Module3.Practice.CollegeManagement.service;

import com.Module3.Practice.CollegeManagement.dto.student.StudentRequestDTO;
import com.Module3.Practice.CollegeManagement.dto.student.StudentResponseDTO;
import com.Module3.Practice.CollegeManagement.entity.AdmissionRecord;
import com.Module3.Practice.CollegeManagement.entity.Student;
import com.Module3.Practice.CollegeManagement.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {
    private final StudentRepository studentRepository;
    private final AdmissionService admissionService;
    private final ModelMapper modelMapper;

    /**
     * Returns Optional to allow Controller to handle 404 responses.
     */
    public Optional<StudentResponseDTO> getStudentById(Long id) {
        return studentRepository.findById(id)
                .map(student -> modelMapper.map(student, StudentResponseDTO.class));
    }

    @Transactional
    public StudentResponseDTO createStudent(StudentRequestDTO studentRequest) {
        // Map Request DTO to Entity
        Student studentEntity = modelMapper.map(studentRequest, Student.class);

        // Use existing admission logic
        AdmissionRecord studentAdmissionRecord = admissionService.initialiseAdmissionRecord(studentEntity);

        // Set the link on the owning side
        studentEntity.setAdmissionRecord(studentAdmissionRecord);

        // Save and map to the specific Response DTO
        Student savedStudent = studentRepository.save(studentEntity);
        return modelMapper.map(savedStudent, StudentResponseDTO.class);
    }

    public List<StudentResponseDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(student -> modelMapper.map(student, StudentResponseDTO.class))
                .toList();
    }

    /**
     * Internal helper for batch operations (still uses entities).
     */
    public List<Student> findAllStudentsById(List<Long> studentIds) {
        return studentRepository.findAllById(studentIds);
    }

    public boolean isStudentIdPresent(long id) {
        return studentRepository.existsById(id);
    }

    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}