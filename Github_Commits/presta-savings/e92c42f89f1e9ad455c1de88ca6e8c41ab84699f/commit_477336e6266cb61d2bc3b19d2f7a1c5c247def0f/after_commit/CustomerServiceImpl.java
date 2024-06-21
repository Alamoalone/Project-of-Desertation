package dev.marvin.savings.customer.service;

import dev.marvin.savings.customer.dao.CustomerDao;
import dev.marvin.savings.customer.dto.CustomerRegistrationRequest;
import dev.marvin.savings.customer.dto.CustomerResponse;
import dev.marvin.savings.customer.dto.CustomerUpdateRequest;
import dev.marvin.savings.customer.model.Customer;
import dev.marvin.savings.customer.model.Role;
import dev.marvin.savings.exception.DuplicateResourceException;
import dev.marvin.savings.exception.advice.ControllerAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService, UserDetailsService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private ControllerAdvice controllerAdvice;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Customer> customer = customerDao.getCustomerByEmail(email);
        Customer existingCustomer;
        if (customer.isPresent()) {
            existingCustomer = customer.get();
            return existingCustomer;
        } else {
            System.out.println("Customer 404");
            throw new UsernameNotFoundException("Customer with email [%s] not found".formatted(email));
        }

    }

    @Override
    public String registerCustomer(CustomerRegistrationRequest registrationRequest) {

        // checks if a customer with the given email already exists in the system
        if (customerDao.existsCustomerWithEmail(registrationRequest.email())) {
            System.out.println(controllerAdvice.processDuplicateResourceException(new DuplicateResourceException("email already taken")));
            throw new DuplicateResourceException("email already taken");
        }

        //Register New Customer
        Customer customer = Customer.builder()
                .memberNumber(generateCustomerMemberNumber())
                .name(registrationRequest.name())
                .email(registrationRequest.email())
                .password(passwordEncoder.encode(registrationRequest.password()))
                .createdDate(System.currentTimeMillis())
                .role(Role.MEMBER)
                .build();

        customerDao.insertCustomer(customer);

        return "customer saved successfully";

    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customerList = customerDao.getAllCustomers();
        List<CustomerResponse> customerDTOList = null;

        if (!customerList.isEmpty()) {
            customerDTOList = new ArrayList<>();
            for (Customer customer : customerList) {
                CustomerResponse customerResponse = mapEntityToDTO(customer);
                customerDTOList.add(customerResponse);
            }
        }
        return customerDTOList;
    }

    @Override
    public Optional<CustomerResponse> getCustomerByMemberNumber(String memberNumber) {
        Optional<Customer> customer = customerDao.getCustomerByMemberNumber(memberNumber);
        CustomerResponse customerResponse;
        if (customer.isPresent()) {
            Customer c = customer.get();
            customerResponse = mapEntityToDTO(c);
            return Optional.ofNullable(customerResponse);
        } else {
            return Optional.empty();
        }
    }

    //TODO: update customer update functionality
    @Override
    public String updateCustomer(String memberNumber, CustomerUpdateRequest customerUpdateRequest) {
        Optional<Customer> customer = customerDao.getCustomerByMemberNumber(memberNumber);
        boolean changes = false;

        String emailUpdate = customerUpdateRequest.email();
        String nameUpdate = customerUpdateRequest.name();
        String mobileUpdate = customerUpdateRequest.mobile();

        Customer update = null;

        if (customer.isPresent()) {
            Customer c = customer.get();
            update = new Customer();

            if (!nameUpdate.isBlank() && !nameUpdate.equalsIgnoreCase(c.getName())) {
                update.setName(nameUpdate);
                changes = true;
            }

            if (!emailUpdate.isBlank() && !emailUpdate.equalsIgnoreCase(c.getEmail())) {
                if (customerDao.existsCustomerWithEmail(emailUpdate)) {
                    throw new DuplicateResourceException("email already exists");
                } else {
                    update.setEmail(emailUpdate);
                    changes = true;
                }
            }

            //TODO: include password, profileImage

            if (!mobileUpdate.isBlank() && !mobileUpdate.equals(c.getMobile())) {
                update.setMobile(mobileUpdate);
                changes = true;
            }
        }

        if (changes) {
            Boolean result = customerDao.updateCustomer(update);
            if (result) {
                return "customer [%s] updated successfully".formatted(memberNumber);
            } else {
                return "error during update";
            }

        } else {
            return "no data changes found";
        }
    }

    @Override
    public String deleteCustomer(String memberNumber) {
        Optional<Customer> customer = customerDao.getCustomerByMemberNumber(memberNumber);

        if (customer.isPresent()) {
            Customer existingCustomer = customer.get();
            customerDao.deleteCustomer(existingCustomer);
            return "customer [%s] deleted successfully".formatted(memberNumber);
        } else {
            return "customer not found";
        }
    }

    private CustomerResponse mapEntityToDTO(Customer customer) {
        return CustomerResponse.builder()
                .memberNumber(customer.getMemberNumber())
                .name(customer.getName())
                .email(customer.getEmail())
                .mobile(customer.getMobile())
                .governmentId(customer.getGovernmentId())
                .build();
    }

    private String generateCustomerMemberNumber() {
        String memberNumber = "MEM" + UUID.randomUUID().toString().substring(0, 6);
        return memberNumber.toUpperCase();
    }

}
