package dev.marvin.savings.customer.dao;

import dev.marvin.savings.customer.model.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    void insertCustomer(Customer customer);

    List<Customer> getAllCustomers();

    Optional<Customer> getCustomerByMemberNumber(String memberNumber);

    Optional<Customer> getCustomerByEmail(String email);

    Boolean updateCustomer(Customer customer);

    void deleteCustomer(Customer customer);

    boolean existsCustomerWithEmail(String email);
}
