import java.math.BigInteger;
import java.security.SecureRandom;

public class Cell {

    public BigInteger previousHash;
    public BigInteger cellHash;
    public String message;
    RSA rsa = new RSA();

    public Cell(String message) {
        this.message = message;
        this.previousHash = null;
        this.cellHash = hash(message);
        System.out.println(cellHash);
    }

    private BigInteger hash(String input) {
        byte[] bytes = input.getBytes();
        BigInteger hashedBigInt = new BigInteger(bytes).modPow(rsa.publicKey, rsa.modulus);
        return hashedBigInt;
    }

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();

        // Generate two large prime numbers (p and q)
        BigInteger p = new BigInteger(512, 100, random);
        BigInteger q = new BigInteger(512, 100, random);

        // Calculate n = p * q (modulus)
        BigInteger n = p.multiply(q);

        // Calculate phi(n) = (p-1) * (q-1) (Euler's totient function)
        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        // Choose a public exponent (e) that is coprime to phi(n)
        BigInteger e = new BigInteger("65537"); // Common choice for e

        // Calculate the private exponent (d) using the modular multiplicative inverse of e
        BigInteger d = e.modInverse(phi);

        System.out.println("Public Key (e, n): " + e + ", " + n);
        System.out.println("Private Key (d, n): " + d + ", " + n);
    }
}

