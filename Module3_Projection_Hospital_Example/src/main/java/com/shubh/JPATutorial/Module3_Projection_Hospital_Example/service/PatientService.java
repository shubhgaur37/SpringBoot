package com.shubh.JPATutorial.Module3_Projection_Hospital_Example.service;


import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.entity.Patient;
import com.shubh.JPATutorial.Module3_Projection_Hospital_Example.repository.PatientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // creates a constructor for dependency injection
public class PatientService {
    private final PatientRepository patientRepository;


    public void testPersistenceWithoutTransactional(Long patientId) {
        // fetching same patient record twice
        Patient p1 = patientRepository.findById(patientId).orElseThrow();
        Patient p2 = patientRepository.findById(patientId).orElseThrow();

        System.out.println("p1: " + p1 + " p2: " + p2);
        // same record persisted into different patient entities
        // using separate db calls which was redundant because we fetched
        // the same patient
        System.out.println("p1 == p2: " + (p1 == p2));
    }

    @Transactional
    public void testPersistenceWithTransactional(Long patientId) {
        // fetching same patient record twice
        Patient p1 = patientRepository.findById(patientId).orElseThrow();
        Patient p2 = patientRepository.findById(patientId).orElseThrow();

        System.out.println("p1: " + p1 + " p2: " + p2);

        // Single DB call, single entity was persisted using a single db call
        // no redundant calls
        System.out.println("p1 == p2: " + (p1 == p2));
    }


    @Transactional
    public void dirtyEntityPersistedToDB(Long patientId) {
        // fetching same patient record twice
        Patient p1 = patientRepository.findById(patientId).orElseThrow();
        Patient p2 = patientRepository.findById(patientId).orElseThrow();

        System.out.println("p1: " + p1 + " p2: " + p2);

        // changes to any entity inside persistence context
        // is commited to DB(synchronized) after the transaction is done
        // triggers an update call, when an entity object is changed inside a transaction
        p1.setName("DirtyName");
    }

    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public void deletePatient(Long patientId) {
        patientRepository.deleteById(patientId);
    }
}
