package ru.zotov.journey;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.zotov.customer.Customer;
import ru.zotov.customer.CustomerRegistrationRequest;
import ru.zotov.customer.CustomerUpdateRequest;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CustomerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    private static final String CUSTOMER_URI = "api/v1/customers";

    @Test
    void canRegisterCustomer() {
        //region Registration request creation
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = UUID.randomUUID() + "@gmail.com";
        Integer age = new Random().nextInt(18, 101);
        CustomerRegistrationRequest customerRegistrationRequest =
                new CustomerRegistrationRequest(name, email, age);
        //endregion

        //region Send post request
        webTestClient.post()
                .uri(CUSTOMER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerRegistrationRequest), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();
        //endregion

        //region Getting all Customers from db
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();
        //endregion

        //region Ensuring that added customer is presented in db
        Customer expectedCustomer = new Customer(name, email, age);

        assertThat(allCustomers)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .contains(expectedCustomer);
        //endregion

        //region Getting Id and query all customers to get added customer
        assert allCustomers != null;
        Integer id = allCustomers
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId).findFirst().orElseThrow();

        expectedCustomer.setId(id);
        webTestClient.get()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<Customer>() {
                })
                .isEqualTo(expectedCustomer);
        //endregion
    }

    @Test
    void canDeleteCustomer() {

        //region Registration request creation
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = UUID.randomUUID() + "@gmail.com";
        Integer age = new Random().nextInt(18, 101);
        CustomerRegistrationRequest customerRegistrationRequest =
                new CustomerRegistrationRequest(name, email, age);
        //endregion

        //region Send post request
        webTestClient.post()
                .uri(CUSTOMER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerRegistrationRequest), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();
        //endregion

        //region Getting all Customers from db
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();
        //endregion

        //region Getting ID and query all customers to get added customer
        assert allCustomers != null;
        Integer id = allCustomers
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId).findFirst().orElseThrow();

        //region Delete customer
        webTestClient.delete()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();
        //endregion

        webTestClient.get()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
        //endregion
    }

    @Test
    void canUpdateCustomer() {
        //region Registration request creation
        Faker faker = new Faker();
        String name = faker.name().fullName();
        String email = UUID.randomUUID() + "@gmail.com";
        Integer age = new Random().nextInt(18, 101);
        CustomerRegistrationRequest customerRegistrationRequest =
                new CustomerRegistrationRequest(name, email, age);
        //endregion

        //region Send post request
        webTestClient.post()
                .uri(CUSTOMER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerRegistrationRequest), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();
        //endregion

        //region Getting all Customers from db
        List<Customer> allCustomers = webTestClient.get()
                .uri(CUSTOMER_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();
        //endregion

        //region Getting ID and query all customers to get added customer
        assert allCustomers != null;
        Integer id = allCustomers
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId).findFirst().orElseThrow();

        //region Update customer
        String newName = UUID.randomUUID().toString();
        CustomerUpdateRequest customerUpdateRequest =
                new CustomerUpdateRequest(newName, null, null);

        webTestClient.put()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(customerUpdateRequest), CustomerIntegrationTest.class)
                .exchange()
                .expectStatus()
                .isOk();
        //endregion

        Customer updatedCustomer = webTestClient.get()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Customer.class)
                .returnResult()
                .getResponseBody();
        //endregion

        Customer expectedCustomer = new Customer(id, newName, email, age);

        assert updatedCustomer != null;
        assertThat(updatedCustomer).isEqualTo(expectedCustomer);
    }
}
