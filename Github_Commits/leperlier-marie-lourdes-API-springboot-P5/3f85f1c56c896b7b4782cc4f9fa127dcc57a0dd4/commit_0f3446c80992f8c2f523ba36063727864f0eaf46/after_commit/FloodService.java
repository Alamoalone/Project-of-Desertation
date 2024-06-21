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



	public List<Object> getListOfHouseHoldByStationNumber(String stationNumber) throws NullPointerException {
		List<Object> listOfHouseHoldOfStationNumber = new ArrayList<Object>();
		List<Map<String, String>> listOfResidentsOfStationNumber = residentsOfStationNumberService
				.getListOfResidentsOfStationNumber(stationNumber);
		

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
			listOfResidentWithMedicalRecord = searchingFullInfoOfResidentsWithMedicalRecord
					.searchInfoOfResident(address);
			listOfHouseHoldOfStationNumber.add(listOfResidentWithMedicalRecord);

		}
		System.out.println(" listOfHouseHoldOfStationNumber" + stationNumber+ listOfHouseHoldOfStationNumber);
		System.out.println("listOfAddress" + listOfAddress);
		
		

		return listOfHouseHoldOfStationNumber;
	}
}
