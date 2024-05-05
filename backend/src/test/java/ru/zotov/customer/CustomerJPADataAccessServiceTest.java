package ru.zotov.customer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Random;

import static org.mockito.Mockito.verify;

class CustomerJPADataAccessServiceTest {

    private CustomerJPADataAccessService underTest;
    @Mock private CustomerRepository customerRepository;
    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new CustomerJPADataAccessService(customerRepository);
    }

    @AfterEach
    void tearDown() {
        try {
            autoCloseable.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void selectAllCustomers() {
        underTest.selectAllCustomers();
        verify(customerRepository).findAll();
    }

    @Test
    void selectCustomerById() {
        int id = 0;
        underTest.selectCustomerById(id);
        verify(customerRepository).findById(id);
    }

    @Test
    void insertCustomer() {
        Customer customer = new Customer(1, "Name", "gmail@gmail.com", 23);
        underTest.insertCustomer(customer);
        verify(customerRepository).save(customer);
    }

    @Test
    void existsCustomerWithEmail() {
        String email = "test@gmailcom";
        underTest.existsCustomerWithEmail(email);
        verify(customerRepository).existsCustomerByEmail(email);
    }

    @Test
    void existsCustomerWithId() {
        Integer id = new Random().nextInt();
        underTest.existsCustomerWithId(id);
        verify(customerRepository).existsCustomerById(id);
    }

    @Test
    void deleteCustomerById() {
        Integer id = new Random().nextInt();
        underTest.deleteCustomerById(id);
        verify(customerRepository).deleteById(id);
    }

    @Test
    void updateCustomer() {
        Customer customer = new Customer(1, "Name", "gmail@gmail.com", 23);
        underTest.updateCustomer(customer);
        verify(customerRepository).save(customer);
    }
}