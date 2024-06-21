package com.safetynet.api.service.dataservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.safetynet.api.model.Person;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonService {
	private final List<Person> persons = new ArrayList<>();

	public Person addPerson(Person person) {
		person.setId(person.getFirstName() + " " + person.getLastName());
		persons.add(person);
		return person;
	}

	public Person updatePerson(String id, Person updatedPerson) throws NullPointerException {
		Person existingPersonUpdated = new Person();
		existingPersonUpdated = persons.stream().filter(person -> person.getId().equals(id)).findFirst()
				.map(existingPerson -> {
					existingPerson.setAddress(updatedPerson.getAddress());
					existingPerson.setCity(updatedPerson.getCity());
					existingPerson.setZip(updatedPerson.getZip());
					existingPerson.setPhone(updatedPerson.getPhone());
					existingPerson.setEmail(updatedPerson.getEmail());
					return existingPerson;
				}).orElseThrow(() -> new NullPointerException(
						"error occured with updating person " + updatedPerson + "not found "));
		return existingPersonUpdated;
	}

	public boolean deleteOnePersonById(String id) throws NullPointerException {
		// log.debug("Deleting medical record for {} {}", firstName, lastName);
		boolean result = persons.removeIf(person -> person.getId().equals(id));

		if (!result) {
			// log.error("Failed to delete person for {} {}", id);
			throw new NullPointerException("this person to remove doesn't exist");
		} else {
			// log.info("Person deleted successfully for {} {}", id);
		}
		return result;
	}

	public Person getOnePersonById(String id) throws NullPointerException {
		Person personFoundById = new Person();
		personFoundById = persons.stream().filter(person -> person.getId().equals(id)).findFirst()
				.map(existingPerson -> {
					return existingPerson;
				}).orElseThrow(() -> new NullPointerException("person not found with id"));

		return personFoundById;
	}

	public List<Person> getPersonsLastName(String lastName) throws NullPointerException {
		List<Person> personsFoundByLastName = new ArrayList<>();
		Iterator<Person> itrPersons = persons.listIterator();
		while (itrPersons.hasNext()) {
			Person itrPerson = itrPersons.next();

			if (itrPerson.getLastName().equals(lastName)) {
				personsFoundByLastName.add(itrPerson);
			}
		}

		if (personsFoundByLastName.isEmpty()) {
			throw new NullPointerException("person not found with lastName");
		}
		System.out.println("personsFoundByLastName" + personsFoundByLastName);
		return personsFoundByLastName;
	}

	public List<Person> getPersonsByAddress(String address) throws NullPointerException {
		List<Person> personsFoundByAddress = new ArrayList<>();
		Iterator<Person> itrPersons = persons.listIterator();
		while (itrPersons.hasNext()) {
			Person itrPerson = itrPersons.next();
			if (itrPerson.getAddress().equals(address)) {
				personsFoundByAddress.add(itrPerson);
			}
		}
		if (personsFoundByAddress.isEmpty()) {
			throw new NullPointerException("person not found with address");
		}
		System.out.println("personsFoundByAddress" + personsFoundByAddress);
		return personsFoundByAddress;
	}

	public List<Person> getPersonsByCity(String city) throws NullPointerException {
		List<Person> personsFoundByCity = new ArrayList<>();
		Iterator<Person> itrPersons = persons.listIterator();
		while (itrPersons.hasNext()) {
			Person itrPerson = itrPersons.next();
			if (itrPerson.getCity().equals(city)) {
				personsFoundByCity.add(itrPerson);
			}
		}
		if (personsFoundByCity.isEmpty()) {
			throw new NullPointerException("person not found with city");
		}
		System.out.println("personsFoundByAddress" + personsFoundByCity);
		return personsFoundByCity;
	}

	public List<Person> getAllPersons() throws NullPointerException {
		if (persons.isEmpty()) {
			throw new NullPointerException("none person registered!");
		}
		System.out.println("Retrieving all persons" + persons);
		return persons;
	}
}