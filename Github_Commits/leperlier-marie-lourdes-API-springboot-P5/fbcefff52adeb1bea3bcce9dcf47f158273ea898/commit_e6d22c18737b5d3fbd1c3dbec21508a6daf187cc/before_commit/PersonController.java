package com.safetynet.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.safetynet.api.model.FireStation;
import com.safetynet.api.model.Person;
import com.safetynet.api.service.dataservice.PersonService;
import com.safetynet.api.utils.IResponseHTTPEmpty;

import jakarta.validation.Valid;

@RestController
public class PersonController implements  IResponseHTTPEmpty {
	@Autowired
	private PersonService personService;

	@PostMapping("/person/")
	public ResponseEntity<Person> createPerson(@Valid @RequestBody Person person) {
		Person personCreated = personService.addPerson(person);
		System.out.println(person);
		return ResponseEntity.status(HttpStatus.CREATED).body(personCreated);
	}

	@PutMapping("/person/{id}")
	public ResponseEntity<?> updateOnePersonById(@RequestBody Person person, @PathVariable String id) {
		Person personFoundById; 
		
		try {
			 personFoundById= personService.updatePerson(id, person);
			return ResponseEntity.status(HttpStatus.CREATED).body( personFoundById);
		} catch (NullPointerException e) {
			// e.printStackTrace();
			// ajouter log error
			System.out.println(e.getMessage());
			return returnResponseEntityEmptyAndCode404();
		}
	}

	@DeleteMapping("/person/{id}")
	public ResponseEntity<Long> deleteOnePersonById(@PathVariable String id)  {
		
		try {
			personService.deleteOnePersonById(id);
			return new ResponseEntity<Long>(HttpStatus.NO_CONTENT);
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			return new ResponseEntity<Long>(HttpStatus.NOT_FOUND);
		}	
	}

	@GetMapping("/person/{id}")
	public ResponseEntity<?> getOnePerson(@PathVariable String id) {
		Person personFoundById ;		
		try {
			personFoundById = personService.getOnePersonById(id);
			return ResponseEntity.status(HttpStatus.OK).body(personFoundById);
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			return returnResponseEntityEmptyAndCode404();
		}
	}

	@GetMapping("/person/")
	public @ResponseBody ResponseEntity<?> getAllPersons() {
		List<Person> allPersons ;
		
		try {
			allPersons = personService.getAllPersons();
			return ResponseEntity.status(HttpStatus.OK).body( allPersons );
		} catch (NullPointerException e) {
			System.out.println(e.getMessage());
			return returnResponseEntityEmptyAndCode404();
		}
	}
}