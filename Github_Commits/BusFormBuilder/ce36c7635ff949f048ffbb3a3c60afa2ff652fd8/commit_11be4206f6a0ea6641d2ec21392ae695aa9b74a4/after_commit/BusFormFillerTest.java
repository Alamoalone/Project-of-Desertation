import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import busformfiller.BusFormFiller;

public class BusFormFillerTest {

	@Test
	public void startTest1() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-09-13");
		LocalDate end = LocalDate.parse("2023-09-15");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "start");
		int expected = 263;
		assertEquals(expected, actual);
	}
	
	@Test
	public void startTest2() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-01-13");
		LocalDate end = LocalDate.parse("2023-09-15");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "start");
		int expected = 24;
		assertEquals(expected, actual);
	}
	
	@Test
	public void startTest3() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-02-16");
		LocalDate end = LocalDate.parse("2023-09-15");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "start");
		int expected = 86;
		assertEquals(expected, actual);
	}
	
	@Test
	public void startTest4() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-04-24");
		LocalDate end = LocalDate.parse("2023-09-15");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "start");
		int expected = 110;
		assertEquals(expected, actual);
	}
	
	@Test
	public void startTest5() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-05-29");
		LocalDate end = LocalDate.parse("2023-09-15");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "start");
		int expected = 172;
		assertEquals(expected, actual);
	}
	
	@Test
	public void startTest6() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-01-01");
		LocalDate end = LocalDate.parse("2023-09-15");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "start");
		int expected = 0;
		assertEquals(expected, actual);
	}
	
	@Test
	public void endTest1() throws IOException {
		var filler = new BusFormFiller();
		LocalDate start = LocalDate.parse("2023-01-09");
		LocalDate end = LocalDate.parse("2023-12-31");
		DateTimeFormatter format = DateTimeFormatter.ofPattern("M/d/yyyy");
		int actual = filler.findLine(new File("src/test/testreport.csv"),start, end, format, "src/test/testreport.csv", "end");
		int expected = 281;
		assertEquals(expected, actual);
	}
	
}
