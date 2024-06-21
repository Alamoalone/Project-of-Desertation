package busformfiller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.commons.io.input.ReversedLinesFileReader;

public class BusFormFiller {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Welcome to the Bus Form Builder. Please enter your name to begin:"); // Intro sentence
		
		// Get Input from the user for their name:
		Scanner sc = new Scanner(System.in);
		String name = sc.nextLine();
		// End of name input
		
		System.out.println("This name will be used for the filled PDF. Is that fine? (Y/N)"); // validate name for PDF
		
		// Create a do-while loop so that our input can be ensured to be yes or no:
		boolean valid;
		do {
			// Get answer
			Scanner in = new Scanner(System.in);
			String answer_for_pdf_name = in.nextLine();
			
			// Validate answer
			switch(answer_for_pdf_name.toUpperCase()) {
				case "Y":
					valid = true; // The input is valid
					getPrestoReport(name);
					break;
				
				case "N":
					valid = true; // The input is valid
					System.out.println("Please enter the name you would like to use for the PDF when it is filled:"); // ask for name
					
					// Get new name
					Scanner ss = new Scanner(System.in);
					name = ss.nextLine();
					
					// Finalize name
					System.out.printf("'%s' will be used.\n", name);
					
					
					break;
				
				default:
					valid = false; // In all other cases, the input is invalid
					System.out.println("Please enter with a Y for Yes and N for No."); // Prompt user again.
					break;
			}
		}while(valid == false);
		
		
	}
	
	public static void getPrestoReport(String name) {
		// Basic formalities for date and report
		System.out.println("Before filling out the Bus Form, I will need the start and end date for the form period.");
		System.out.println("Please specify the start date. Be sure to include the day, month and year "
				+ "(In that order, no commas or periods)."); // ask for start date
		
		try {
			// Get input for start date
			Scanner scan = new Scanner(System.in);
			String startDate = scan.nextLine();
			DateTimeFormatter form = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH); // Uniform Formatter for input
			DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy", Locale.ENGLISH); // Uniform Formatter for output
			LocalDate start_date = LocalDate.parse(startDate, form); // This statement will parse the string into a proper Date object to be formatted
			startDate = start_date.format(format); // Save the formatted date back into the original string
			System.out.printf("Start date: %s\n", startDate); // Prints to user
			
			// Get input for end date
			System.out.println("Please specify the end date. Be sure to include the day, month and year "
					+ "(In that order, no commas or periods).");
			Scanner scanner = new Scanner(System.in);
			String endDate = scan.nextLine();
			LocalDate end_date = LocalDate.parse(endDate, form); // We can reuse the same formatting
			endDate = end_date.format(format); // Save the formatted date back into the original string
			System.out.printf("End date: %s\n", endDate); // Prints to user
			
			if(startDate.equals(endDate)) { // Check to see if start and end are the same
				System.out.println("Sorry, but your start date and end date cannot be the same. Please try with different dates."); // print error
				System.exit(0); // Exit program
			}else if(end_date.isBefore(start_date)) { // Check to see if the start date is after the end date
				System.out.println("Sorry, the end date cannot be before the start date. Please try a different date.");
				System.exit(0);
			}
			// Get the Presto Transit Usage Report
			System.out.println("Please provide the path to the most recent presto transit usage report for the required year. Please make sure it is a .csv file: (Don't have a report?"
					+ " You can acquire one from your online Presto Account.)");
			Scanner str = new Scanner(System.in);
			String path = str.nextLine().replace("\"", "");
			File file = new File(path);
			
			// Scan the .csv file
			Scanner scan_csv = new Scanner(file);
			
			// Go through the .csv file and find which line has the required start date
			int start_count = findLine(file, start_date, end_date, format, path, "start"); // This will count how many lines it takes to get to the start date.
			int end_count = findLine(file,start_date, end_date, format, path, "end"); // This will count how many lines it takes to get to the end date.
			
			System.out.println(start_count);
			System.out.println(end_count);
			Stream<String> lines = Files.lines(Paths.get(path)); // Gains a full stream of the file lines
			// Skips start_count amount of lines and ends at the end_count
			String[] fileLines = lines.skip(start_count).limit(end_count-start_count).toArray(String[] :: new);
			int row_index = end_count - start_count; // stores row_index of the former 2D array
			String[][] presto_report_array = new String[row_index][4]; // Our main 2D array which stores the Presto transit data
			
			for(int j = 0; j < row_index; j++) { // Loop to access the rows of the 2D array
				for(int i = 0; i < 4; i++) { // Loop to access the columns of the 2D array
					presto_report_array[j][i] = filter(fileLines[j])[i]; // populates the 2D array by filtering the fileLines String[]
					// The reason for the filter is because we don't need the location, it is a waste of space
					// Then we access each index of the filtered array with 'i'
				}
			}
			addDates(presto_report_array);
		}catch(DateTimeException e) { // Catch a date error due to improper user input
			e.printStackTrace();
		}catch(IOException r) {
			System.out.println("Uh-oh! Seems like the file may not be avaialable. For more info: " + r);
		}
	}
	
	/*
	 * @param line		A string that should be comma-delimited
	 * @return finishedArray	An array that has filtered the unnecessary values and splits the String by commas.
	 */
	public static String[] filter(String line) {
		ArrayList<String> arr = new ArrayList<String>();
		String[] temp = line.split(",");
		for(String i : temp){
			arr.add(i);
		}
		arr.remove(2);
		String[] finishedArray = new String[4];
		finishedArray = arr.toArray(finishedArray);
		return finishedArray;
	}
	
	/*
	 * @param file	The file from which the Scanner will read from
	 * @param startDate	The start date specified by the user for the Presto report
	 * @param endDate	The end date specified by the user for the Presto report
	 * @param format	The DateTimeFormatter being used by the program
	 * @param direction	Meant to specify whether the user wishes to start with the start date or end date
	 * @throws IOException	If file is not found, throw IOException
	 * @return lineCount	Returns how many lines are required to reach the start date	
	 */
	public static int findLine(File file, LocalDate startDate, LocalDate endDate, DateTimeFormatter format, String path, String direction) throws IOException {
		switch(direction) {
			case "start":
				int lineCount = 0; // Meant to count how many lines it takes to get to the required start date
				int dateCount = 0; // Increments the date by 1 each time
				String start = ""; // String variable to store the changing dates
				/*
				 * The next piece of code is essential for the search to work
				 * 1: The outer do-while loop is meant to increment the dates by a certain count each time.
				 * 2: The scanner is renewed in every iteration so that a fresh date may be compared against the report.
				 * 3: The inner while loop remains mostly simple, a loop through the file lines and finding the right value.
				 * This algorithm will work with both end and start dates.
				 */
				do{
					Scanner sc = new Scanner(file); // Fresh scanner for the file every time loop iterates
					start = startDate.plusDays(dateCount).format(format); // Stores the formatted date with any day increments
					lineCount = 0; // At every new iteration the lineCount is reset so that it does not overflow from the previous date
					while(sc.hasNextLine()){ // While loop to iterate file lines
						String line = sc.nextLine(); // Read line
						if(line.contains(start)){ // Control flow
							return lineCount; // return the lineCount, how many lines it took
						}
						lineCount++; // Otherwise, increment lineCount
					}
					dateCount++; // Increment to next date
				}while(!start.equals(endDate.format(format))); // Ensure that the start date does not equal the end date
				System.out.println("It seems this date does not exist. Please make sure it is included in the presto report. Date: " + startDate.format(format));
				// If there is no such date in the report, then it must not exist even after incrementing till the end date
				return lineCount; // In all cases return the lineCount
			case "end":
				try (ReversedLinesFileReader r = ReversedLinesFileReader.builder().setPath(path).get()){
					int endLineCount = 0; // Meant to count how many lines it takes to get to the required end date
					int endDateCount = 0; // Increments the date by 1 each time
					String end = ""; // String variable to store the changing dates
					String line;
					do{
						end = endDate.minusDays(endDateCount).format(format);
						endLineCount = 0;
						while(r != null) {
							line = r.readLine();
							if(line.contains(end)) {
								return getTotalLines(path) - endLineCount;
							}
							endLineCount++;
						}
						endDateCount++;
					}while(!end.equals(startDate.format(format))); // Ensure that the end date does not equal the start date
					System.out.println("It seems this date does not exist. Please make sure it is included in the presto report. Date: " + startDate.format(format));
					// If there is no such date in the report, then it must not exist even after incrementing till the end date
					return endLineCount; // In all cases return the lineCount
				}
			default:
				System.out.println("Error: The value for 'direction' must be 'start' or 'end'");
				break;
		}
		return 0; // Just a formality to satisfy Java
		
	}
	
	/*
	 * @param p_r_a		The presto report array
	 */
	public static void addDates(String[][] p_r_a) {
		// Here will add days of the week to the corresponding dates so that we can work with the PDF document better
		DateTimeFormatter form = DateTimeFormatter.ofPattern("EEEE"); // Gets pattern for days of the week
		DateTimeFormatter prev_form = DateTimeFormatter.ofPattern("M/d/yyyy"); // Gets formatting for original dates
		for(int i = 0; i < p_r_a.length; i++) { // Loops through 2D Array
			p_r_a[i][0] = p_r_a[i][0].substring(0, 9).replaceAll(" ", ""); // Gets the date only, removes the whitespace and the time
			LocalDate day = LocalDate.parse(p_r_a[i][0], prev_form); // Parses the date
			p_r_a[i][0] = p_r_a[i][0] + ": " + day.format(form); // Adds a day with the original date
			p_r_a[i][3] = p_r_a[i][3].replaceAll("-", ""); // Removes the negative from the fare payment, just an extra addition
			System.out.println(p_r_a[i][0]);
		}
	}
	
	/*
	 * @param path	Requires a file path to read from
	 * @return count	Returns the total line count
	 */
	public static int getTotalLines(String path) throws IOException {
		return (int) Files.lines(Paths.get(path)).count();
	}
	
	/*
	 * @param file	Get the file to read from
	 * @return line	Returns the last line of the file
	 */
	public static String getLastLine(File file) throws IOException{
		Scanner sc = new Scanner(file);
		String line = "";
		while(sc.hasNextLine()) {
			line = sc.nextLine();
		}
		return line;
	}

}
