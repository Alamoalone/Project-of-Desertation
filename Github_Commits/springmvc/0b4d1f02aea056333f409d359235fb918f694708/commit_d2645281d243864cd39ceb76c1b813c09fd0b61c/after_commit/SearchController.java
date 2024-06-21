package com.springmvcimplant.ioc.controllers;

import org.springframework.core.convert.ConversionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.view.RedirectView;

import com.springmvcimplant.ioc.entities.Student;

@Controller
@RequestMapping("/search")
public class SearchController {
	
	@RequestMapping("/user/{userid}/{username}")
	public String getUserDetail(@PathVariable("userid") int id,@PathVariable("username") String username) {
		System.out.println(id);
		System.out.println(username);
		return "home";
	}
	
	@RequestMapping("/lookup")
	public RedirectView search(@RequestParam("querybox") String query) {
		String url = "https://www.google.co.in?q="+query;
		RedirectView redirectView = new RedirectView();
		redirectView.setUrl(url);
		return redirectView;
	}
	
	@RequestMapping("/nullexception")
	public String nullException() {
		System.out.println("Triggering Null Exception");
		String str = null;
		System.out.println(str.length());
		return "home";
	}
	
	@RequestMapping("/numberexception")
	public String numberException() {
		System.out.println("Triggering Number Exception");
		String str = "Gaurav";
		Integer.parseInt(str);
		return "home";
	}

	@RequestMapping("/home")
	public String home() {
		System.out.println("Going to Home");
		return "home";
	}
	
	@RequestMapping("/form")
	public String form() {
		System.out.println("Going to Form");
		return "form";
	}
	
	@RequestMapping(path="/handle",method = RequestMethod.POST)
//	public String handle(@RequestParam Map<String, String> reqParam) {
	public String handle(@ModelAttribute("student") Student student,BindingResult result) {
		if(result.hasErrors()) {
			return "form";
		}
		System.out.println("Going to Handle");
//		System.out.println(reqParam);
		System.out.println(student.toString());
		System.out.println(student.getAddress());
		return "success";
	}
	
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({NullPointerException.class})
	public String NullExceptionHandler(Model m) {
		System.out.println("Null Pointer Exception Occurred");
		m.addAttribute("msg","Null Pointer Exception");
		return "null_error";
	}
	
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({ConversionException.class,NumberFormatException.class})
	public String ConversionExceptionHandler(Model m) {
		System.out.println("Number Format Exception Occurred");
		m.addAttribute("msg","Number Format Exception");
		return "null_error";
	}
	
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Exception.class})
	public String GenericExceptionHandler(Model m) {
		System.out.println("General Exception Occurred");
		m.addAttribute("msg","General Exception");
		return "null_error";
	}
}
