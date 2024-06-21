import static org.junit.Assert.*;

import org.junit.Test;

public class MainTest {
    @Test
    public void testIsPrimeTrue() {
        // Arrange
        // Act
        boolean output = Main.isPrime(5); // include name of class
        // Assert
        assertTrue(output); 
    }

    @Test
    public void testIsPrimeFalse() {
        // Arrange
        // Act
        boolean output = Main.isPrime(4);
        // Assert
        assertFalse(output);
    }

    @Test
    public void testIsPrimeNumberOne(){
        //Arrange
        //Act
        boolean output = Main.isPrime(1);
        //Assert
        assertFalse(output);
    }

    @Test
    public void testIsPrimeNegativeNumber(){
        //Arrange
        //Act
        boolean output = Main.isPrime(-1);
        //Assert
        assertFalse(output);
    }

    @Test

     public void testIsPrimeHighNumber(){
        //Arrange
        //Act
        boolean output = Main.isPrime(97);
        //Assert
        assertTrue(output);
    }
}
