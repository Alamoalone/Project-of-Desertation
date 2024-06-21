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
        // Arrange
        CustomerRequestDto requestDto = new CustomerRequestDto("zander","zander@gmail.com", LocalDate.of(2000,4,2), Occupation.Developer);
        doNothing().when(customerDetailService).createCustomerDetailsRecord(requestDto);

        // Act
        ResponseEntity<?> responseEntity = customerDetailsController.createCustomerDetails(requestDto);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testCreateCustomerDetails_AlreadyExists() {
        // Arrange
        CustomerRequestDto requestDto = new CustomerRequestDto();
        doThrow(new CustomerDetailsAlreadyExistsException("")).when(customerDetailService).createCustomerDetailsRecord(requestDto);

        // Act and Assert
        assertThrows(CustomerDetailsAlreadyExistsException.class, () -> customerDetailsController.createCustomerDetails(requestDto));
    }

    @Test
    void testGetCustomerDetailsByEmail_Success() {
        // Arrange
        String email = "test@example.com";
        CustomerResponseDto responseDto = new CustomerResponseDto();
        when(customerDetailService.getCustomerDetailsByEmail(email)).thenReturn(responseDto);

        // Act
        ResponseEntity<CustomerResponseDto> responseEntity = customerDetailsController.getCustomerDetailsByEmail(email);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void testGetCustomerDetailsByEmail_NotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(customerDetailService.getCustomerDetailsByEmail(email)).thenThrow(new ResourceNotFoundException("", "", ""));

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> customerDetailsController.getCustomerDetailsByEmail(email));
    }

    @Test
    void testUpdateCustomerDetails_Success() {
        // Arrange
        CustomerRequestDto requestDto = new CustomerRequestDto();
        when(customerDetailService.updateCustomerDetailsRecord(requestDto)).thenReturn(true);

        // Act
        ResponseEntity<?> responseEntity = customerDetailsController.updateCustomerDetails(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testUpdateCustomerDetails_Failure() {
        // Arrange
        CustomerRequestDto requestDto = new CustomerRequestDto();
        when(customerDetailService.updateCustomerDetailsRecord(requestDto)).thenReturn(false);

        // Act
        ResponseEntity<?> responseEntity = customerDetailsController.updateCustomerDetails(requestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteCustomerByEmail_Success() {
        // Arrange
        String email = "test@example.com";
        when(customerDetailService.deleteCustomerDetailsByEmail(email)).thenReturn(true);

        // Act
        ResponseEntity<?> responseEntity = customerDetailsController.deleteCustomerByEmail(email);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testDeleteCustomerByEmail_Failure() {
        // Arrange
        String email = "test@example.com";
        when(customerDetailService.deleteCustomerDetailsByEmail(email)).thenReturn(false);

        // Act
        ResponseEntity<?> responseEntity = customerDetailsController.deleteCustomerByEmail(email);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
}

