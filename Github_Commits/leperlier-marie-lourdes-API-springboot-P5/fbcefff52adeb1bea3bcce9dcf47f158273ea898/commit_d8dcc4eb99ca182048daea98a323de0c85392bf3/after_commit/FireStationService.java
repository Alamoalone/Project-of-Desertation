package com.safetynet.api.service.dataservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.safetynet.api.model.FireStation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FireStationService {
	private List<FireStation> fireStations = new ArrayList<>();

	public FireStation addFireStation(FireStation fireStation) {
		fireStation.setId(fireStation.getStationNumber());
		fireStations.add(fireStation);
		return fireStation;
	}

//ajouter illagal state argumnt pour le body vide passé en parametre sans entrée et envoyer un code erreur de non creation de donnée ,
//car les annotation permette de renvoyer erreur 400
	public FireStation addStationNumberOfExistingFireStation(FireStation fireStation, String address) {
		FireStation fireStationByAddress = new FireStation();
		try {
			fireStationByAddress = getOneFireStationByAddress(address);
			fireStations.removeIf(
					fireStationByAddressToRemove -> fireStationByAddressToRemove.getAddress().equals(address));

			fireStation.setId(fireStation.getStationNumber());
			fireStation.setAddress(fireStationByAddress.getAddress());
			fireStationByAddress.setStationNumber(fireStation.getStationNumber());
			fireStationByAddress.setId(fireStation.getId());
			fireStation = fireStationByAddress;
			fireStations.add(fireStation);
			return fireStation;
		} catch (NullPointerException e) {
			// System.out.println("aucun element crée");
			throw new NullPointerException(
					" Error has occured creating firestation with new station number,this address doesn't exist");

		}
	}

	public FireStation addAddressOfExistingFireStation(FireStation fireStation, String stationNumber)
			throws NullPointerException {
		FireStation createdFireStation = new FireStation();

		try {
			List<FireStation> fireStationsByStationNumber = getFireStationsById(stationNumber);
			fireStations.removeIf(fireStationByAddressToRemove -> fireStationByAddressToRemove.getStationNumber()
					.equals(stationNumber));
			createdFireStation = fireStationsByStationNumber.stream()
					.filter(fireStationByStationNumber -> fireStationByStationNumber.getId().equals(stationNumber))
					.findFirst().map(existingFireStation -> {
						existingFireStation.setId(fireStation.getStationNumber());
						existingFireStation.setAddress(fireStation.getAddress());
						// existingFireStation.setAddress(updatedFireStation.getAddress());
						return existingFireStation;
					}).orElse(null);
			fireStations.add(createdFireStation);

		} catch (NullPointerException e) {
			// ajouter log debug pour ce message avec printsacktrace
			// System.out.println(e.getMessage());
			throw new NullPointerException(
					" Error has occured creating  firestation with new address,this station number doesn't exist");

		}

		/*
		 * }catch(Exception e) { e.printStackTrace(); throw new
		 * NoSuchElementException("aucun element trouvé");
		 */
		return createdFireStation;
	}

	// modification uniquement de la station number et non l'addresse
	public FireStation updateFireStation(String id, FireStation updatedFireStation) throws NullPointerException {
		FireStation existingFireStationUpdated = new FireStation();
		existingFireStationUpdated = fireStations.stream().filter(fireStation -> fireStation.getId().equals(id))
				.findFirst().map(existingFireStation -> {
					existingFireStation.setStationNumber(updatedFireStation.getStationNumber());
					existingFireStation.setId(updatedFireStation.getStationNumber());
					return existingFireStation;
				}).orElseThrow(() -> new NullPointerException(
						"error occured with updating firestation " + updatedFireStation + "not found "));

		return existingFireStationUpdated;
	}

	public boolean deleteFireStationById(String id) throws NullPointerException {
		// log.debug("Deleting medical record for {} {}", firstName, lastName);
		boolean result = false;
		result = fireStations.removeIf(fireStation -> fireStation.getId().equals(id));

		if (!result) {
			// log.error("Failed to delete firestation for {} {}", id);
			throw new NullPointerException("this station number of firestation to remove doesn't exist");
		} else {
			// log.info("firestation deleted by id with station number successfully for {}
			// {}", id);
		}

		return result;
	}

	public boolean deleteOneFireStationByAddress(String address) throws NullPointerException {
		// log.debug("Deleting medical record for {} {}", firstName, lastName);
		boolean result = false;

		result = fireStations.removeIf(fireStation -> fireStation.getAddress().equals(address));
		// log.info("firestation deleted by address with station number successfully for
		// {} {}", id);
		if (!result) {
			// log.error("Failed to delete firestation for {} {}", id);
			throw new NullPointerException("this address of firestation to remove doesn't exist");
		}
		return result;
	}

	public List<FireStation> getFireStationsById(String id) throws NullPointerException {
		List<FireStation> fireStationsFoundById = new ArrayList<>();
		Iterator<FireStation> itrFireStations = fireStations.listIterator();
		while (itrFireStations.hasNext()) {
			FireStation itrFireStation = itrFireStations.next();
			if (itrFireStation.getId().equals(id)) {
				fireStationsFoundById.add(itrFireStation);
			}
		}

		if (fireStationsFoundById.isEmpty()) {
			throw new NullPointerException("firestation not found");
		}
		System.out.println("fireStationsFoundById" + fireStationsFoundById);
		return fireStationsFoundById;

	}

	public FireStation getOneFireStationByAddress(String address) throws NullPointerException {
		FireStation fireStationFoundByAddress = new FireStation();
		fireStationFoundByAddress = fireStations.stream()
				.filter(fireStation -> fireStation.getAddress().equals(address)).findAny().map(existingFireStation -> {
					return existingFireStation;
				}).orElseThrow(() -> new NullPointerException("firestation not found "));
		return fireStationFoundByAddress;
	}

	public List<FireStation> getAllFireStations() throws NullPointerException {
		if (fireStations.isEmpty()) {
			throw new NullPointerException("none firestation registered!");
		}
		System.out.println("Retrieving all persons" + fireStations);
		return fireStations;
	}
}
