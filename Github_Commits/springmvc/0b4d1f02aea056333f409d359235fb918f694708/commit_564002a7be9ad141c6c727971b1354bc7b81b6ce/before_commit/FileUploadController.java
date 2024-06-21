package com.springmvcimplant.ioc.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

@Controller
@RequestMapping("/file")
public class FileUploadController {

	@RequestMapping("/fileform")
	public String showUploadForm() {
		return "fileform";
	}

	@RequestMapping(value = "/uploadimage", method = RequestMethod.POST)
	public String uploadImage(@RequestParam("profile") CommonsMultipartFile file, HttpSession s,Model m) {
		System.out.println("File Upload Handler");
		System.out.println("File Size " + file.getSize());
		System.out.println("File Content Type " + file.getContentType());
		System.out.println("File Name " + file.getName());
		System.out.println("Original File Name " + file.getOriginalFilename());
		System.out.println("File Storage Description " + file.getStorageDescription());
		byte[] data = file.getBytes();
		String path = s.getServletContext().getRealPath("/") + "WEB-INF" + File.separator + "resources" + File.separator
				+ "image" + File.separator + file.getOriginalFilename();
		System.out.println("Context Path "+path);
		try {
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(data);
			fos.close();
			System.out.println("File Uploaded");
			m.addAttribute("filename",file.getOriginalFilename());
			m.addAttribute("msg","Uploaded Successfully");
		} catch (IOException e) {
			m.addAttribute("msg","Upload Failed");
			e.printStackTrace();
		}

		return "filesuccess";
	}
}
