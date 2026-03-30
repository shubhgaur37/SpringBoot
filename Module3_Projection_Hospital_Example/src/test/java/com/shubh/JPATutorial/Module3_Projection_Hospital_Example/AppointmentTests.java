package com.shubh.JPATutorial.Module3_Projection_Hospital_Example;


import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Appointment;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Doctor;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.type.BloodGroupType;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service.AppointmentService;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service.DoctorService;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
public class AppointmentTests {
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private DoctorService doctorService;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void initTestData(){
        // Better test pattern to create the patient and persist it in DB first before running the test
        patient = Patient.builder()
                .name("Shubh")
                .dob(LocalDate.of(2000,2,22))
                .email("shubh@gaur.com")
                .bloodGroup(BloodGroupType.B_POSITIVE)
                .build();

        patient = patientService.savePatient(patient); // added to persistence context

        doctor = Doctor.builder()
                .name("YASh")
                .specialization("ORTHO")
                .email("yash@pandey.com")
                .build();

        doctor = doctorService.saveDoctor(doctor);// added to persistence context

        appointment = new Appointment();
        appointment.setAppointmentTime(LocalDateTime.of(2026,5,5,16,30));
        appointment.setReason("Shoulder Pain");

    }

    @Test
    void createAppointment(){
        var updatedAppointment = appointmentService.createNewAppointment(appointment,doctor.getId(),patient.getId());
        System.out.println(updatedAppointment);
    }

    @Test
    void createAppointmentAndDeletePatient(){
        doctor.setEmail("yash@create.com");
        doctor = doctorService.saveDoctor(doctor); // added to persistence context
        
        var updatedAppointment = appointmentService.createNewAppointment(appointment,doctor.getId(),patient.getId());
        System.out.println(updatedAppointment);
        patientService.deletePatient(patient.getId());
        System.out.println("Appointment Exists After Patient Deleted : " + appointmentService.doesAppointmentExist(updatedAppointment.getId()));
    }


}
