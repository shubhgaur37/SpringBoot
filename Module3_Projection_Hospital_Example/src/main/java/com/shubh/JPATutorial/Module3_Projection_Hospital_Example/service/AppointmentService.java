package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Appointment;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Doctor;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.AppointmentRepository;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.DoctorRepository;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Appointment createNewAppointment(Appointment appointment, Long doctorId, Long patientId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow();
        Patient patient = patientRepository.findById(patientId).orElseThrow();

        // set appointment
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        // Appointment currently in transient state, make it persistent
        return appointmentRepository.save(appointment);
    }

    public boolean doesAppointmentExist(Long appointmentId) {
        return appointmentRepository.existsById(appointmentId);
    }
}
