import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
    public BigInteger privateKey;
    public BigInteger publicKey;
    public BigInteger modulus;

    // Generate key pair
    public void generateKeyPair(int bitLength) {
        SecureRandom random = new SecureRandom();
        BigInteger p = new BigInteger(bitLength / 2, 100, random);
        BigInteger q = new BigInteger(bitLength / 2, 100, random);
        modulus = p.multiply(q);

        BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

        publicKey = new BigInteger("65537"); // Commonly used public exponent
        privateKey = publicKey.modInverse(phi);
    }

    // Encrypt plaintext string
    public BigInteger encrypt(String plaintext) {
        byte[] bytes = plaintext.getBytes();
        BigInteger message = new BigInteger(bytes);
        return message.modPow(publicKey, modulus);
    }

    // Decrypt ciphertext to string
    public String decrypt(BigInteger ciphertext) {
        BigInteger decryptedMessage = ciphertext.modPow(privateKey, modulus);
        byte[] bytes = decryptedMessage.toByteArray();
        return new String(bytes);
    }

    public static void main(String[] args) {
        RSA rsa = new RSA();

        // Generate key pair with 2048-bit length
        rsa.generateKeyPair(2048);

        // Example usage with string
        String originalMessage = "Hello, RSA!";
        System.out.println("Original message: " + originalMessage);

        // Encryption
        BigInteger encryptedMessage = rsa.encrypt(originalMessage);
        System.out.println("Encrypted message: " + encryptedMessage);

        // Decryption
        String decryptedMessage = rsa.decrypt(encryptedMessage);
        System.out.println("Decrypted message: " + decryptedMessage);
    }
}
