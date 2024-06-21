package edu.cs2430.assignment3;
public class Course {
   /**
    * The current index for the array of prerequisite courses.
    */
   private int currentPrerequisiteIndex;
   /**
    * A constant to represent the maximum size (5) of the prerequisite course array
    */
   private static final int MAX_PREREQUISITES = 5;
   /**
    * The course name.
    */
   private String name;
   /**
    * The course number.
    */
   private int number;
   /**
    * The number of credits for the course.
    */
   private int numberOfCredits;
   /**
    * An array of prerequisite courses.
    */
   private Course[] prerequisiteCourses;
   /**
    * The program the course belongs to
    */
   private Program program;

   public Course(String name, int number, Program program, int numberOfCredits){
       this.name  = name;
       this.number = number;
       this.program = program;
       this.numberOfCredits = numberOfCredits;
   }
   public Course(String name, int number, Program program, int numberOfCredits, Course[] prerequisiteCourses){
       this.name  = name;
       this.number = number;
       this.program = program;
       this.numberOfCredits = numberOfCredits;
       this.prerequisiteCourses = new Course[MAX_PREREQUISITES];
       for(int i=0;i<prerequisiteCourses.length;i++){
           this.prerequisiteCourses[i] = prerequisiteCourses[i];
       }
   }
   public boolean addPrerequisiteCourse(Course prerequisiteCourse){
       int temp = 0;
       for(Course c : prerequisiteCourses){
           if (c != null) {
               temp += 1;
           }
       }
       if(validateAddPrerequisiteCourse(prerequisiteCourse)){
           prerequisiteCourses[temp] = prerequisiteCourse;
           return true;
       }
       else{
           return false;
       }
   }
   public boolean containsPrerequisite(Course prerequisiteCourse){
       boolean contains = false;
       for (Course c : prerequisiteCourses){
           if (c == prerequisiteCourse) {
               contains = true;
               break;
           }
       }
       return contains;
   }
   public boolean containsPrerequisiteCycle(Course prerequisiteCourse){
         boolean contains = false;
         for (Course c : prerequisiteCourses){
              if (c == prerequisiteCourse) {
                contains = true;
                break;
              }
              else{
                contains = c.containsPrerequisiteCycle(prerequisiteCourse);
              }
         }
         return contains;
   }
   //I couldn't figure this one out.
   public String getName(){return this.name;}
   public int getNumber(){return this.number;}
   public int getNumberOfCredits(){return this.numberOfCredits;}
   public Course[] getPrerequisiteCourses(){return prerequisiteCourses;}
   public Program getProgram(){return program;}
   public void setName(String name){this.name = name;}
   public void setNumber(int number){this.number = number;}
   public void setNumberOfCredits(int numberOfCredits){this.numberOfCredits = numberOfCredits;}
   public void setProgram(Program program){this.program = program;}
   private boolean validateAddPrerequisiteCourse(Course prerequisiteCourse){
       boolean valid = true;
       if(prerequisiteCourse == null){
           valid = false;
       } else {
            if(prerequisiteCourse.name.equals(this.name) && prerequisiteCourse.program.equals(this.program)){
                valid = false;
            }
            int prerequisiteCoursesInArray = 0;
            for(int i=0;i<prerequisiteCourses.length;i++){
                if(prerequisiteCourses[i] != null){
                    prerequisiteCoursesInArray += 1;
                }
            }
            if(prerequisiteCoursesInArray>4){
                valid = false;
            }
            for (Course c : prerequisiteCourses){
                if (c == prerequisiteCourse) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
   }
}
