package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service;

import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Insurance;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.InsuranceRepository;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;
    private final PatientRepository patientRepository;

    /**
     * Associates an Insurance entity with an existing Patient.
     * <p>
     * Note on Hibernate States:
     * 1. The 'patient' retrieved via repository is in the MANAGED state.
     * 2. The 'insurance' passed as an argument is in the TRANSIENT state.
     * </p>
     *
     * @param insurance The transient Insurance object to be linked.
     * @param patientId The ID of the patient already existing in the DB.
     * @return The updated Insurance object.
     * @throws RuntimeException if patientId is not found.
     */
    @Transactional
    public Insurance assignInsuranceToPatient(Insurance insurance, Long patientId) {

        // Fetch patient: object enters the Persistence Context (MANAGED state).
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with id: " + patientId));

        /*
         * --- RELATIONSHIP MAPPING & CASCADING ---
         * * Problem: 'patient' is MANAGED, but 'insurance' is TRANSIENT (not in DB).
         * Without 'cascade = CascadeType.PERSIST' on Patient, Hibernate won't save insurance.
         * Result: TransientPropertyValueException (Managed entity cannot reference Transient entity).
         */

        // 1. Set the insurance on the patient (Owning side updates the Foreign Key).
        patient.setInsurance(insurance);

        /*
         * --- WHY SETTING THE BACK-REFERENCE IS GOOD PRACTICE ---
         * * Even though 'insurance.setPatient(patient)' is technically optional for the DB
         * (because Patient is the owner), it is crucial for "Domain Model Integrity":
         * * 1. In-Memory Consistency: If you pass this 'insurance' object to another component
         * within this same request, calling insurance.getPatient() would return NULL
         * if not set here, leading to NullPointerExceptions in business logic.
         * 2. Unit Testing: If testing this logic without a DB (Mocking), the link
         * must be set manually to verify behavior.
         * 3. Cache Synchronization: Ensures the L1 Cache (Persistence Context) holds
         * a fully navigable graph.
         */
        insurance.setPatient(patient);

        /*
         * --- CASCADING BEHAVIOR AFTER DETACHMENT ---
         *
         * Once this method returns and the Transaction commits, 'patient' and 'insurance'
         * become DETACHED (no longer tracked by the Persistence Context).
         * * 1. If you modify 'patient' or 'insurance' while DETACHED, changes won't hit the DB.
         * 2. Re-attachment: If you call patientRepository.save(detachedPatient) later:
         * - If CascadeType.MERGE is set: The detached Insurance will also be merged.
         * - If CascadeType.MERGE is MISSING: Only the Patient fields are merged; the
         * Insurance link might be lost or the Insurance state might stay stale in DB.
         */

        return insurance;
    }

    @Transactional
    public Insurance updateInsurance(Insurance insurance, Long patientId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        patient.setInsurance(insurance);
        return insurance;
    }

    @Transactional
    public Patient removeInsurance(Long patientId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow();
        patient.setInsurance(null);
        return patient;
    }

    public boolean doesInsuranceExists(Long id) {
        return insuranceRepository.existsById(id);
    }
}