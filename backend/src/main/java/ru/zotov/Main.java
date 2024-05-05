package ru.zotov;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.zotov.customer.Customer;
import ru.zotov.customer.CustomerRepository;
import java.util.Random;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(CustomerRepository customerRepository){
        return args -> {
            Random random = new Random();
            Faker faker = new Faker();
            Customer customer = new Customer(
                    faker.name().firstName(),
                    faker.internet().safeEmailAddress(),
                    random.nextInt(19,81)
            );
            customerRepository.save(customer);
        };
    }
}
