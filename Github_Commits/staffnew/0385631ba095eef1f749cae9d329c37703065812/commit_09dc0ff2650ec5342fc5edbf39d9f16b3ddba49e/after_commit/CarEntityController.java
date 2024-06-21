package com.example.staff.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import com.example.staff.entity.CarEntity;
import com.example.staff.service.CarEntityService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins="http://localhost:8084/")
@RestController
@SessionScope
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CarEntityController {

	private final CarEntityService carEntityService;
	
	@GetMapping("/cars/{id}")
	public ResponseEntity<CarEntity> getCarEntityById(@PathVariable Long id){
		try {
			return ResponseEntity.ok(carEntityService.getCarEntityById(id));
		} catch(Exception ex) {return new ResponseEntity<>(HttpStatus.NOT_FOUND);}
	}
		
	@GetMapping("/cars")
	public ResponseEntity<List<CarEntity>> findByNumberModel(@RequestParam(required=false) String filter){
		if(filter!=null && filter.trim().length()!=0) {
			String filter1=filter;
			String filter2=filter;
			try {
				return ResponseEntity.ok(carEntityService.findByNumberModel(filter1,filter2));
			} catch (Exception ex) {return new ResponseEntity<>(HttpStatus.NOT_FOUND);}
		}else {
			try {
				return ResponseEntity.ok(carEntityService.getAllCarEntity());
			} catch (Exception ex) {return new ResponseEntity<>(HttpStatus.NOT_FOUND);}
		}
	}
	
	@PostMapping("/cars")
	public ResponseEntity<CarEntity> addCarEntity(@RequestBody CarEntity carEntity){
		try {
			return new ResponseEntity<>(carEntityService.addCarEntity(carEntity),HttpStatus.CREATED);
		}catch(Exception ex) {return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);}
	}
	
	@PutMapping("/cars/{id}")
	public ResponseEntity<CarEntity> editCarEntity(@PathVariable Long id, @RequestBody CarEntity carEntity){
		try {
			return new ResponseEntity<>(carEntityService.editCarEntity(id, carEntity),HttpStatus.OK);
		}catch(Exception ex) {return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);}
	}
	
	@DeleteMapping("/cars/{id}")
	public ResponseEntity<HttpStatus> deleteCarEntityById(@PathVariable Long id){
		try {
			carEntityService.deleteCarEntityById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}catch(Exception ex) {return new ResponseEntity<>(HttpStatus.NOT_FOUND);}
	}
}
