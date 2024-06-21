package com.safetynet.api.service.dataservice;

//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.safetynet.api.model.MedicalRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordService {
    private final List<MedicalRecord> medicalRecords = new ArrayList<>();

    public MedicalRecord addMedicalRecord(MedicalRecord medicalRecord){
      //  log.debug("Adding medical record: {}", medicalRecord.getFirstName() + " " + medicalRecord.getLastName());
    	medicalRecord.setId(
    			medicalRecord.getFirstName() + " " +medicalRecord.getLastName() );
      
       	medicalRecords.add(medicalRecord);

     //   log.info("Medical record added successfully: {}", medicalRecord.getFirstName() + " " + medicalRecord.getLastName());
        return medicalRecord;
    }

    public MedicalRecord updateMedicalRecord (String id, MedicalRecord updatedMedicalRecord) {
       // log.debug("Updating medical record for: {}", firstName + " " + lastName);
        return medicalRecords.stream()
                .filter(medicalRecord -> medicalRecord.getId().equals(id))
                .findFirst()
                .map(existingMedicalRecord -> {
                    existingMedicalRecord.setBirthdate(updatedMedicalRecord.getBirthdate());
                    existingMedicalRecord.setMedications(updatedMedicalRecord.getMedications());
                    existingMedicalRecord.setAllergies(updatedMedicalRecord.getAllergies());
                    return existingMedicalRecord;
                })
                .orElse(null);
    }

    public boolean deleteOneMedicalRecordById(String id) {
     //   log.debug("Deleting medical record for {} {}", id);
        boolean result = medicalRecords.removeIf(medicalRecord -> medicalRecord.getId().equals(id) );
        if (result) {
         //   log.info("Medical record deleted successfully for {} {}", id);
        } else {
           // log.error("Failed to delete medical record for {} {}", id);
        }
        return result;
    }

    public MedicalRecord getOneMedicalRecordById(String id) {
    	  return medicalRecords.stream()
                  .filter(medicalRecord -> medicalRecord.getId().equals(id))
                  .findFirst()
                  .map(existingMedicalRecord -> { 
                	  return existingMedicalRecord;
                  } )
                  .orElse(null);
     }
    public List<MedicalRecord> getAllMedicalRecords() {
       // log.debug("Retrieving all medical records");
    	
    	if(medicalRecords ==null) {
    		return null;
    	}
    	System.out.println("Retrieving all medical records"+medicalRecords);
        return medicalRecords;
    }
}