package com.arjunagi.customer_details_management_service.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.arjunagi.customer_details_management_service.dtos.CustomerRequestDto;
import com.arjunagi.customer_details_management_service.dtos.CustomerResponseDto;
import com.arjunagi.customer_details_management_service.exception.CustomerDetailsAlreadyExistsException;
import com.arjunagi.customer_details_management_service.exception.ResourceNotFoundException;
import com.arjunagi.customer_details_management_service.models.Occupation;
import com.arjunagi.customer_details_management_service.services.ICustomerDetailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)

class CustomerDetailsControllerTest {


    @Mock
    private ICustomerDetailService customerDetailService;

    @InjectMocks
    private CustomerDetailsController customerDetailsController;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    void testCreateCustomerDetails_Success() {
        CustomerRequestDto requestDto = new CustomerRequestDto("zander","zander@gmail.com", LocalDate.of(2000,4,2), Occupation.Developer);
        doNothing().when(customerDetailService).createCustomerDetailsRecord(requestDto);

        ResponseEntity<?> responseEntity = customerDetailsController.createCustomerDetails(requestDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testCreateCustomerDetails_AlreadyExists() {
        CustomerRequestDto requestDto = new CustomerRequestDto();
        doThrow(new CustomerDetailsAlreadyExistsException("")).when(customerDetailService).createCustomerDetailsRecord(requestDto);

        assertThrows(CustomerDetailsAlreadyExistsException.class, () -> customerDetailsController.createCustomerDetails(requestDto));
    }

    @Test
    void testGetCustomerDetailsByEmail_Success() {
        String email = "test@gmail.com";
        CustomerResponseDto responseDto = new CustomerResponseDto();
        when(customerDetailService.getCustomerDetailsByEmail(email)).thenReturn(responseDto);

        ResponseEntity<CustomerResponseDto> responseEntity = customerDetailsController.getCustomerDetailsByEmail(email);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void testGetCustomerDetailsByEmail_NotFound() {
        String email = "nonexistent@gamil.com";
        when(customerDetailService.getCustomerDetailsByEmail(email)).thenThrow(new ResourceNotFoundException("", "", ""));

        assertThrows(ResourceNotFoundException.class, () -> customerDetailsController.getCustomerDetailsByEmail(email));
    }

    @Test
    void testUpdateCustomerDetails_Success() {
        CustomerRequestDto requestDto = new CustomerRequestDto();
        when(customerDetailService.updateCustomerDetailsRecord(requestDto)).thenReturn(true);

        ResponseEntity<?> responseEntity = customerDetailsController.updateCustomerDetails(requestDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testUpdateCustomerDetails_Failure() {
        CustomerRequestDto requestDto = new CustomerRequestDto();
        when(customerDetailService.updateCustomerDetailsRecord(requestDto)).thenReturn(false);

        ResponseEntity<?> responseEntity = customerDetailsController.updateCustomerDetails(requestDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteCustomerByEmail_Success() {
        String email = "test@gmail.com";
        when(customerDetailService.deleteCustomerDetailsByEmail(email)).thenReturn(true);

        ResponseEntity<?> responseEntity = customerDetailsController.deleteCustomerByEmail(email);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteCustomerByEmail_Failure() {
        String email = "test@gmail.com";
        when(customerDetailService.deleteCustomerDetailsByEmail(email)).thenReturn(false);

        ResponseEntity<?> responseEntity = customerDetailsController.deleteCustomerByEmail(email);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
}

