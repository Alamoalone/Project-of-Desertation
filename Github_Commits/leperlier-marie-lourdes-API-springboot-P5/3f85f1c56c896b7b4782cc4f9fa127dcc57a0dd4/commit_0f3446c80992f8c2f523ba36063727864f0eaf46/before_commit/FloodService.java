package com.safetynet.api.service.alertssafetynetservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FloodService {
	@Autowired
	ResidentsOfStationNumberService residentsOfStationNumberService;

	@Autowired
	SearchingFullInfoOfResidentsByAddressWithMedicalRecordImpl searchingFullInfoOfResidentsWithMedicalRecord;

private List<Object> listOfHouseHoldOfStationNumber = new ArrayList<Object>();

	public List<Object> getListOfHouseHoldByStationNumber(String stationNumber) throws NullPointerException {
		List<Map<String, String>> listOfResidentsOfStationNumber =new ArrayList<Map<String, String>>();
	try {
		 listOfResidentsOfStationNumber = residentsOfStationNumberService
				.getListOfResidentsOfStationNumber(stationNumber); 
	}catch (NullPointerException e) {
		throw new NullPointerException("not resident  found  of this firestation "+stationNumber+"   to get its HouseHold and  prevent them for flood");
	}
		 

		// creating list of address commun between addresses found in each info of resident
		ListIterator<Map<String, String>> itrResidentsOfStationNumber = listOfResidentsOfStationNumber.listIterator();
		List<String> listOfAddress = new ArrayList<String>();
		while (itrResidentsOfStationNumber.hasNext()) {
			Map<String, String> itrResidentNext = itrResidentsOfStationNumber.next();

			if (!listOfAddress.contains(itrResidentNext.get("address"))) {
				listOfAddress.add(itrResidentNext.get("address"));
			}
		}
		
		// getting medicalrecords searching with address of each resident
		for (String address : listOfAddress) {
			List<Map<String, String>> listOfResidentWithMedicalRecord = new ArrayList<Map<String, String>>();
			// gathering all HouseHold of firestation by address in list created in each iteration on address of the  list of address
			listOfResidentWithMedicalRecord = searchingFullInfoOfResidentsWithMedicalRecord
					.searchInfoOfResident(address);
			
			listOfHouseHoldOfStationNumber.add(listOfResidentWithMedicalRecord);

		}
		System.out.println(" listOfHouseHoldOfStationNumber" + listOfHouseHoldOfStationNumber);
		System.out.println("listOfAddress" + listOfAddress);
		
		return listOfHouseHoldOfStationNumber;
	}
}
