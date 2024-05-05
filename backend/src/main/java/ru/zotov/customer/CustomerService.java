package ru.zotov.customer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.zotov.exception.RequestValidationException;
import ru.zotov.exception.ResourceAlreadyExistsException;
import ru.zotov.exception.ResourceNotFoundException;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(
            @Qualifier("jdbc_repository") CustomerDao customerDao
    ) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers(){
        return customerDao.selectAllCustomers();
    }

    public Customer getCustomerById(Integer customerId){
        return customerDao.selectCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with ID: %d not found".formatted(customerId)
                ));
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest){
        if (customerDao.existsCustomerWithEmail(customerRegistrationRequest.email())){
            throw new ResourceAlreadyExistsException(
                    "customer with inserted email already exists"
            );
        }

        customerDao.insertCustomer(
                new Customer(
                        customerRegistrationRequest.name(),
                        customerRegistrationRequest.email(),
                        customerRegistrationRequest.age()
                )
        );
    }

    public void deleteCustomerById(Integer customerId) {
        if (!customerDao.existsCustomerWithId(customerId)){
            throw new ResourceNotFoundException(
                    "customer with ID: %d not found".formatted(customerId
                    ));

        }

        customerDao.deleteCustomerById(customerId);
    }

    public void updateCustomer(Integer customerId,
                               CustomerUpdateRequest customerUpdateRequest) {
        Customer customer = getCustomerById(customerId);

        boolean changes = false;

        if (customerUpdateRequest.name() != null &&
                !customerUpdateRequest.name().equals(customer.getName())){
            customer.setName(customerUpdateRequest.name());
            changes = true;
        }

        if (customerUpdateRequest.email() != null &&
                !customerUpdateRequest.email().equals(customer.getEmail())){

            if (customerDao.existsCustomerWithEmail(customerUpdateRequest.email())){
                throw new ResourceAlreadyExistsException("email already taken");
            }

            customer.setEmail(customerUpdateRequest.email());
            changes = true;
        }

        if (customerUpdateRequest.age() != null &&
                !customerUpdateRequest.age().equals(customer.getAge())){
            customer.setAge(customerUpdateRequest.age());
            changes = true;
        }

        if (!changes){throw new RequestValidationException("nothing to update");}

        customerDao.updateCustomer(customer);
    }
}
