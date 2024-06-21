//package main.java.ca.mcmaster.se2aa4.mazerunner;
package ca.mcmaster.se2aa4.mazerunner;
import java.io.IOException;

public class Path {

    public String user_path;
    private String[] can_path; //CREATE A METHOD TO TURN CMD LINE ARGUMENTS INTO CANONICAL PATH
    public Maze maze;
    public boolean status;
    private String direction = "EAST";

    // make another class that deals separately (UserPath, CompPath)

    //constructor
    public Path(String path, Maze maze){

        this.user_path = path;
        this.maze = maze; // calls the string version of the matrix
    }

    //public static void canonical() {
    //}
    public static void compute() { // find a working path on the maze, maybe have different class

    }
    public void status() {

        boolean status_east=false, status_west=false;
        try{
            // from East -> West, go through eastern entry and end at western entry
            status_east = isPathValid("WEST", maze.getEEast(), maze.getEWest());
            
            //if (status_east); 
        } catch(IndexOutOfBoundsException e){
            //logger.info("/!\\No -p arguments given/!\\");
            System.out.println("Error: East to West path does not work.");
        }

        try{
            // from West -> East, go through western entry and end at eastern exit 
            status_west = isPathValid("EAST", maze.getEWest(), maze.getEEast());
            
        
            //if (status_west){}
        } catch(IndexOutOfBoundsException e){
            System.out.println("Error: West to East path does not work.");
        }

        // print statements if path exists or doesn't
    }
    public boolean isPathValid(String d, int[] entry, int[] exit) throws IndexOutOfBoundsException{

        direction = d;
        int[] start = entry;
        int[] end = exit;
        //              [x, y] location
        int[] current = start;

        for (int i=0; i<can_path.length; i++){

            if (direction.equals("EAST")){
                if (can_path[i].equals("F")){
                    if ((maze.string_matrix)[current[0]][current[1]+1]=="PASS"){
                        current[1] += 1;
                    }
                    else{return false;}
                }
                else{
                    if (can_path[i].equals("R")){
                        direction = "SOUTH";
                    }
                    else if (can_path[i].equals("L")){
                        direction = "NORTH";
                    }
                }
            }
            else if (direction.equals("SOUTH")){
                if (can_path[i].equals("F")){
                    if ((maze.string_matrix)[current[0]+1][current[1]]=="PASS"){
                        current[0] += 1;
                    }
                    else{return false;}
                }
                else{
                    if (can_path[i].equals("R")){
                        direction = "WEST";
                    }
                    else if (can_path[i].equals("L")){
                        direction = "EAST";
                    }
                }
            }
            else if (direction.equals("NORTH")){
                if (can_path[i].equals("F")){
                    if ((maze.string_matrix)[current[0]-1][current[1]]=="PASS"){
                        current[0] -= 1;
                    }
                    else{return false;}
                }
                else{
                    if (can_path[i].equals("R")){
                        direction = "EAST";
                    }
                    else if (can_path[i].equals("L")){
                        direction = "WEST";
                    }
                }
            }
            else if (direction.equals("WEST")){
                if (can_path[i].equals("F")){
                    if ((maze.string_matrix)[current[0]][current[1]-1]=="PASS"){
                        current[1] -= 1;
                    }
                    else{return false;}
                }
                else{
                    if (can_path[i].equals("R")){
                        direction = "NORTH";
                    }
                    else if (can_path[i].equals("L")){
                        direction = "SOUTH";
                    }
                }
            }   
        }
        
        if ((current[0] == end[0])&&(current[1] == end[1])){
            return true;
        }
        else{
            return false;
        }

    }
    public static void CtoF() { // maybe make separate paths 
    }

    
}
