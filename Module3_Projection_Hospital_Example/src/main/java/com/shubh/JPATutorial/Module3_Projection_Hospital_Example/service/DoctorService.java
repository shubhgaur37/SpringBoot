package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Doctor;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }
}
