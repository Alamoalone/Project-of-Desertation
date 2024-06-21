package org.example;

import org.example.dto.Department;
import org.example.dto.SchoolManagementSystem;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        SchoolManagementSystem c1 = new SchoolManagementSystem();

        c1.addDepartment("Computer science");

       //c1.printDepartment();

        //c1.printStudent();

        //c1.findDepartment("DOO1");

        //c1.findCourse("C001");

        c1.addCourse("Intro to programing", 2.0, "DOO1");

        c1.findCourse("COO1");

        c1.addStudent("s", "o","DOO1");


        c1.registerCourse("SOO1", "COO1");
        System.out.println(c1.findCourse("COO1"));

        c1.findStudent("SOO1");

        c1.addCourse("Arrays", 2.0,"DOO1");


        c1.hasRegisteredForThisCourse("SOO1", "COO2");

    }
}
