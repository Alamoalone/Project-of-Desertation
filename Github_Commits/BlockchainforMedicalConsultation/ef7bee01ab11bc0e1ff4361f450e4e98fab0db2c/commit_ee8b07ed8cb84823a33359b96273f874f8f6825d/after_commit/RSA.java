import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
public class RSA {
    public BigInteger privateKey;
    public BigInteger publicKey;
    public BigInteger modulus;
    public FileIO file;
    public static int counter=0;
    public static int j=0;
    public static BigInteger[] PreviousHash = new  BigInteger[5];
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

    public static  void Block(String originalMessage){

        RSA rsa = new RSA();
          rsa.generateKeyPair(2048);
         

        System.out.println("Block #"+ counter);
        if(counter ==0) System.out.println("Previous Hash:null");
       // if(counter!=0) System.out.println("Previous Hash:"); 
        System.out.println("Original message: " + originalMessage);



        // Encryption
        BigInteger encryptedMessage = rsa.encrypt(originalMessage);
        System.out.println("Hash: " + encryptedMessage);

         PreviousHash[j] = encryptedMessage;
         
        if (counter !=0){
            
            System.out.println("Previous Hash:"+PreviousHash[j-1]);

        }


        // Decryption
        String decryptedMessage = rsa.decrypt(encryptedMessage);
        System.out.println("Decrypted message: " + decryptedMessage);
        counter++;
        j++;
        }
    

    public static void main(String[] args) {
        RSA rsa = new RSA();

        // Generate key pair with 2048-bit length
        rsa.generateKeyPair(2048);
        String[] arr = FileIO.getPatientMessage();
        Block(arr[0]);
        Block(arr[1]);
        Block(arr[2]);
        Block(arr[3]);
        Block(arr[4]);
    }


    }
