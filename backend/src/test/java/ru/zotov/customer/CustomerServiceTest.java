package ru.zotov.customer;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.zotov.exception.RequestValidationException;
import ru.zotov.exception.ResourceAlreadyExistsException;
import ru.zotov.exception.ResourceNotFoundException;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private CustomerService underTest;
    @Mock
    CustomerDao customerDao;

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDao);
    }

    @Test
    void getAllCustomers() {
        underTest.getAllCustomers();
        verify(customerDao).selectAllCustomers();
    }

    @Test
    void canGetCustomerById() {
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));

        Customer actual = underTest.getCustomerById(customer.getId());
        assertThat(actual).isEqualTo(customer);
    }

    @Test
    void willThrowsWhenGetCustomerByIdReturnsEmptyOptional() {
        int id = new Random().nextInt();
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with ID: %d not found".formatted(id));
    }

    @Test
    void addCustomer() {
        String email = getRandomEmail();
        when(customerDao.existsCustomerWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest customerRegistrationRequest = getCustomerRegistrationRequest(email);

        underTest.addCustomer(customerRegistrationRequest);
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).insertCustomer(customerArgumentCaptor.capture());
        Customer capturedValue = customerArgumentCaptor.getValue();

        assertThat(capturedValue.getId()).isNull();
        assertThat(capturedValue.getName()).isEqualTo(customerRegistrationRequest.name());
        assertThat(capturedValue.getEmail()).isEqualTo(customerRegistrationRequest.email());
        assertThat(capturedValue.getAge()).isEqualTo(customerRegistrationRequest.age());
    }

    @Test
    void willThrowWhenEmailExistsWhileAddingCustomer() {
        String email = "Random_email@gmail.com";

        when(customerDao.existsCustomerWithEmail(email)).thenReturn(true);
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(
                "Alex", email, new Random().nextInt()
        );

        assertThatThrownBy(() -> underTest.addCustomer(customerRegistrationRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessage("customer with inserted email already exists");

        verify(customerDao, never()).insertCustomer(any());
    }

    @Test
    void deleteCustomerById() {
        int id = new Random().nextInt();

        when(customerDao.existsCustomerWithId(id)).thenReturn(true);

        underTest.deleteCustomerById(id);
        verify(customerDao).deleteCustomerById(id);
    }

    @Test
    void willThrowWhenIdNotExistsWhileDeletingCustomerWithId() {
        int id = new Random().nextInt();

        when(customerDao.existsCustomerWithId(id)).thenReturn(false);

        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with ID: %d not found".formatted(id)
                );

        verify(customerDao, never()).deleteCustomerById(id);
    }

    @Test
    void canUpdateAllCustomerProperties() {
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));

        CustomerUpdateRequest customerUpdateRequest = getCustomerUpdateRequest();

        when(customerDao.existsCustomerWithEmail(customerUpdateRequest.email())).thenReturn(false);

        underTest.updateCustomer(customer.getId(), customerUpdateRequest);

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getAge()).isEqualTo(customerUpdateRequest.age());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customerUpdateRequest.email());
        assertThat(capturedCustomer.getName()).isEqualTo(customerUpdateRequest.name());
    }

    @Test
    void canUpdateCustomerName() {
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));

        CustomerUpdateRequest customerUpdateRequest =
                new CustomerUpdateRequest(UUID.randomUUID().toString(), null, null);

        underTest.updateCustomer(customer.getId(), customerUpdateRequest);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getName()).isEqualTo(customerUpdateRequest.name());
    }

    @Test
    void canUpdateCustomerEmail() {
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));

        CustomerUpdateRequest customerUpdateRequest =
                new CustomerUpdateRequest(null, getRandomEmail(), null);

        when(customerDao.existsCustomerWithEmail(customerUpdateRequest.email())).thenReturn(false);
        underTest.updateCustomer(customer.getId(), customerUpdateRequest);

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customerUpdateRequest.email());
        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
    }

    @Test
    void canUpdateCustomerAge() {
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));

        CustomerUpdateRequest customerUpdateRequest =
                new CustomerUpdateRequest(null, null, new Random().nextInt());

        underTest.updateCustomer(customer.getId(), customerUpdateRequest);

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getAge()).isEqualTo(customerUpdateRequest.age());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
    }

    @Test
    void willThrowWhenUpdateCustomerEmailWhileEmailAlreadyTaken() {
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));

        String newEmail = getRandomEmail();

        CustomerUpdateRequest customerUpdateRequest =
                new CustomerUpdateRequest(null, newEmail, null);

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(true);

        assertThatThrownBy(() -> underTest.updateCustomer(customer.getId(), customerUpdateRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class).hasMessage("email already taken");

        verify(customerDao, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenCustomerUpdateHasNoChanges(){
        Customer customer = getRandomCustomer();
        when(customerDao.selectCustomerById(customer.getId())).thenReturn(Optional.of(customer));
        CustomerUpdateRequest customerUpdateRequest =
                new CustomerUpdateRequest(customer.getName(), customer.getEmail(), customer.getAge());

        assertThatThrownBy(() -> underTest.updateCustomer(customer.getId(), customerUpdateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("nothing to update");

        verify(customerDao, never()).updateCustomer(any());
    }

    private @NotNull Customer getRandomCustomer() {
        String name = UUID.randomUUID().toString();
        String email = name + "@gmail.com";
        int age = new Random().nextInt(18, 100);

        return new Customer(name, email, age);
    }

    private @NotNull String getRandomEmail() {
        return UUID.randomUUID() + "gmail.com";
    }

    private @NotNull CustomerUpdateRequest getCustomerUpdateRequest() {
        String name = UUID.randomUUID().toString();
        String email = name + "@gmail.com";
        int age = new Random().nextInt(18, 100);

        return new CustomerUpdateRequest(name, email, age);
    }

    private @NotNull CustomerRegistrationRequest getCustomerRegistrationRequest(String email) {
        String name = UUID.randomUUID().toString();
        int age = new Random().nextInt(18, 100);

        return new CustomerRegistrationRequest(name, email, age);
    }
}