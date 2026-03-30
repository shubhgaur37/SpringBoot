package com.shubh.JPATutorial.Module3_Projection_Hospital_Example;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Insurance;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.type.BloodGroupType;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service.InsuranceService;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class InsuranceTests {
    @Autowired
    private PatientService patientService;

    @Autowired
    private InsuranceService insuranceService;

    private Patient patient; // setting up the patient reference
    private Insurance insurance;

    @BeforeEach
    void savePatient(){
        // Better test pattern to create the patient and persist it in DB first before running the test
        patient = Patient.builder()
                .name("Shubh")
                .dob(LocalDate.of(2000,2,22))
                .email("shubh@gaur.com")
                .bloodGroup(BloodGroupType.B_POSITIVE)
                .build();

        // transient state insurance
        insurance = Insurance.builder()
                .provider("HDFC ERGO")
                .policyNumber("HDFC_230")
                .validUntil(LocalDate.of(2030,1,1))
                .build();

    }

    @Test
    void testAssignInsurancePatient(){
        patient = patientService.savePatient(patient); // added to persistence context

        // automatically infers the type at compile time(not dynamic typing), introduced in java 10
        var updatedInsurance = insuranceService.assignInsuranceToPatient(insurance, patient.getId());

        System.out.println(updatedInsurance);
    }

    @Test
    void testDeletePatientWithInsuranceRemovesBothCascading(){
        patient.setEmail("new@1234532.com");
        patient = patientService.savePatient(patient); // added to persistence context
        insurance.setPolicyNumber("HDFC_NEW_TEST");
        var updatedInsurance = insuranceService.assignInsuranceToPatient(insurance, patient.getId());

        // Action: Delete Parent
        patientService.deletePatient(patient.getId());

        /* * CASCADE BEHAVIOR:
         * If CascadeType.REMOVE is set, Hibernate deletes Insurance automatically.
         * After Detachment: If the transaction is closed, objects are no longer
         * tracked. Re-attaching requires CascadeType.MERGE to sync child updates.
         */
        System.out.println("Insurance Exists in DB: " + insuranceService.doesInsuranceExists(updatedInsurance.getId()));
    }

    @Test
    void testUpdateInsuranceForPatientRemovesOrphanedInsurance(){
        patient.setEmail("new@key.com");
        patient = patientService.savePatient(patient); // added to persistence context
        insurance.setPolicyNumber("HDFC_NEW_ORPHAN");

        var oldInsurance = insuranceService.assignInsuranceToPatient(insurance, patient.getId());

        // Action: Update Insurance
        Insurance newInsurance = Insurance.builder()
                .provider("Reliance")
                .policyNumber("Reliance_230")
                .validUntil(LocalDate.of(2030,1,1))
                .build();

        var updatedInsurance = insuranceService.updateInsurance(newInsurance, patient.getId());

        System.out.println("Orphan Insurance Exists in DB: " + insuranceService.doesInsuranceExists(oldInsurance.getId()));
        System.out.println("Updated Insurance exists in DB: " + insuranceService.doesInsuranceExists(updatedInsurance.getId()));
    }

    @Test
    void testRemoveInsuranceForPatientRemovesOrphanedInsurance(){
        patient.setEmail("fdsgds@key.com");
        patient = patientService.savePatient(patient); // added to persistence context
        insurance.setPolicyNumber("HDFC_NEW_ORPHAN2");

        var oldInsurance = insuranceService.assignInsuranceToPatient(insurance, patient.getId());
        var patientWithoutInsurance = insuranceService.removeInsurance(patient.getId());

        System.out.println("Orphan Insurance Exists in DB: " + insuranceService.doesInsuranceExists(oldInsurance.getId()));
        System.out.println(patient);
    }
}
