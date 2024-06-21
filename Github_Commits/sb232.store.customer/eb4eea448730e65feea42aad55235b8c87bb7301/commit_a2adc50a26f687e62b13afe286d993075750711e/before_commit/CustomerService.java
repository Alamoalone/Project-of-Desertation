package store.customer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    
    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> list() {
        return StreamSupport.stream(customerRepository.findAll().spliterator(), false)
            .collect(Collectors.toList())
            .stream().map(CustomerModel::to).toList();
    }

    public Customer find(String id) {
        return customerRepository.findById(id).orElse(null).to();
    }

    public void delete(String id) {
        customerRepository.deleteById(id);
    }

    public Customer create(Customer in) throws RuntimeException {
        if (in.password() == null)
            throw new RuntimeException("Password is mandatory");
        in.password(in.password().trim());
        if (in.password().length() < 5)
            throw new RuntimeException("Password is shorter than 5 characters");
        try {
            // Transformando o password em hash password
            final String hash = calculateHash(in.password());
            in.hashPassword(hash);
            in.password(null);
            return customerRepository.save(new CustomerModel(in)).to();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Customer findBy(String email, String password) {
        try {
            email = email.trim();
            final String hash = calculateHash(password.trim());
            return customerRepository.findFirstByEmailHash(email, hash).map(CustomerModel::to).orElse(null);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateHash(String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        byte[] encoded = Base64.getEncoder().encode(hash);
        return new String(encoded);
    }

}
