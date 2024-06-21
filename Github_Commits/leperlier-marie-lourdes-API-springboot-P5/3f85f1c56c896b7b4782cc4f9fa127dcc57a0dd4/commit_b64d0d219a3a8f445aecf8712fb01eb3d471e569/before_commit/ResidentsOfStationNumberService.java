package com.safetynet.api.service.alertssafetynetservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResidentsOfStationNumberService {
	@Autowired
	SearchingInfoOfResidentOfStationNumberImpl infoOfResidentOfStationNumber;

	@Autowired
	SortingAdultsAndChildsOfListOfResidentsWithCountDown countDownOfAdultsAndChilds;

	private List<Map<String, String>> listOfResidentOfStationNumber;

	public List<Map<String, String>> getListOfResidentsOfStationNumber(String stationNumber)
			throws NullPointerException {
		try {
			listOfResidentOfStationNumber = new ArrayList<Map<String, String>>();
			listOfResidentOfStationNumber = infoOfResidentOfStationNumber.searchInfoOfResident(stationNumber);
		} catch (NullPointerException e) {
			throw new NullPointerException("Residents not found at this station number");
		}

		for (Map<String, String> residents : listOfResidentOfStationNumber) {
			residents.remove("age");
			System.out.println("residents" + residents);
		}

		System.out.println("list of residents" + listOfResidentOfStationNumber);
		return listOfResidentOfStationNumber;
	}

	public Map<String, Integer> sortAdultsAndChildsOfListOfResidentsWithCountDown(String stationNumber)
			throws NullPointerException {
		Map<String, Integer> mapCountDownOfAdultsAndChilds = countDownOfAdultsAndChilds
				.sortAdultsAndChilds(stationNumber);
		if (mapCountDownOfAdultsAndChilds.isEmpty()) {
			throw new NullPointerException(
					"Error has occured sorting Adults and childs  because not found at this station number");
		}
		return mapCountDownOfAdultsAndChilds;
	}
}
