package ru.zotov.customer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.zotov.AbstractTestContainers;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerJDBCDataAccessServiceTest extends AbstractTestContainers {

    private CustomerJDBCDataAccessService underTest;
    private final CustomerRowMapper customerRowMapper =
            new CustomerRowMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerJDBCDataAccessService(
                getJdbcTemplate(), customerRowMapper
        );
    }

    @Test
    void selectAllCustomers() {
        Customer customer = new Customer("Alex",
                UUID.randomUUID() + "@gmail.com",
                new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);

        List<Customer> customers = underTest.selectAllCustomers();

        assertThat(customers).isNotEmpty();
    }

    @Test
    void selectCustomerById() {
        String email = UUID.randomUUID() + "@gmail.com";

        Customer customer = new Customer("Alex",
                email,
                new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getId()).isEqualTo(id);
                    assertThat(c.getName()).isEqualTo(customer.getName());
                    assertThat(c.getEmail()).isEqualTo(customer.getEmail());
                    assertThat(c.getAge()).isEqualTo(customer.getAge());
                });
    }

    @Test
    void willReturnEmptyWhenSelectCustomerById() {
        int id = -1;

        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isEmpty();
    }

    @Test
    void insertCustomer() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, 20);

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent().hasValueSatisfying(c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(customer.getEmail());
            assertThat(c.getAge()).isEqualTo(customer.getAge());
        });
    }

    @Test
    void existsCustomerWithEmail() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);
        boolean actual = underTest.existsCustomerWithEmail(email);
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerWithEmailReturnFalse() {
        String email = UUID.randomUUID() + "@gmail.com";
        assertThat(underTest.existsCustomerWithEmail(email)).isFalse();
    }

    @Test
    void existsCustomerWithId() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        boolean actual = underTest.existsCustomerWithId(id);

        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerWithIdReturnFalse() {
        int id = -1;
        boolean actual = underTest.existsCustomerWithId(id);

        assertThat(actual).isFalse();
    }

    @Test
    void deleteCustomerById() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();
        underTest.deleteCustomerById(id);
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isEmpty();
    }

    @Test
    void updateCustomerName() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        String newName = "foo";
        Customer update = new Customer();
        update.setId(id);
        update.setName(newName);

        underTest.updateCustomer(update);
        Optional<Customer> actual = underTest.selectCustomerById(id);
        assertThat(actual).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getId()).isEqualTo(id);
                    assertThat(c.getName()).isEqualTo(newName);
                    assertThat(c.getEmail()).isEqualTo(customer.getEmail());
                    assertThat(c.getAge()).isEqualTo(customer.getAge());
                });
    }

    @Test
    void updateCustomerEmail() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, new Random().nextInt(20, 90));

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        String newEmail = UUID.randomUUID() + "@gmail.com";
        Customer update = new Customer();
        update.setId(id);
        update.setEmail(newEmail);

        underTest.updateCustomer(update);
        Optional<Customer> actual = underTest.selectCustomerById(id);
        assertThat(actual).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getId()).isEqualTo(id);
                    assertThat(c.getName()).isEqualTo(customer.getName());
                    assertThat(c.getEmail()).isEqualTo(newEmail);
                    assertThat(c.getAge()).isEqualTo(customer.getAge());
                });
    }

    @Test
    void updateCustomerAge() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, 20);

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        int newAge = 100;
        Customer update = new Customer();
        update.setId(id);
        update.setAge(newAge);

        underTest.updateCustomer(update);
        Optional<Customer> actual = underTest.selectCustomerById(id);
        assertThat(actual).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getId()).isEqualTo(id);
                    assertThat(c.getName()).isEqualTo(customer.getName());
                    assertThat(c.getEmail()).isEqualTo(customer.getEmail());
                    assertThat(c.getAge()).isEqualTo(newAge);
                });
    }

    @Test
    void updateCustomerAllFields() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, 20);

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        String newName = "foo";
        String newEmail = UUID.randomUUID() + "@gmail.com";
        int newAge = 100;
        Customer update = new Customer();
        update.setId(id);
        update.setName(newName);
        update.setEmail(newEmail);
        update.setAge(newAge);

        underTest.updateCustomer(update);
        Optional<Customer> actual = underTest.selectCustomerById(id);
        assertThat(actual).isPresent().hasValue(update);
    }

    @Test
    void willNotUpdateIfNothingToUpdate() {
        String email = UUID.randomUUID() + "@gmail.com";
        Customer customer = new Customer("Alex",
                email, 20);

        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        Customer update = new Customer();
        update.setId(id);

        underTest.updateCustomer(update);
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c.getId()).isEqualTo(id);
                    assertThat(c.getName()).isEqualTo(customer.getName());
                    assertThat(c.getEmail()).isEqualTo(customer.getEmail());
                    assertThat(c.getAge()).isEqualTo(customer.getAge());
                });
    }
}