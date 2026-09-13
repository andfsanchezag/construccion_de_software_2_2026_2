package application.domain.services.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;

class UpdateCustomerServiceTest {

    private CustomerServiceHarness harness;

    @BeforeEach
    void setUp() {
        harness = new CustomerServiceHarness();
    }

    private UpdateCustomerService service() {
        return new UpdateCustomerService(
                harness.customerRepository,
                harness.authorizeCustomerOperation,
                harness.registerOperationAndAudit);
    }

    @Test
    void employeeUpdatesCustomerInformation() {
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("4001"));
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("4001");
        desired.setName("Updated Name");
        desired.setEmail("updated@example.com");
        User employee = CustomerTestSupport.employeeUser(1);

        Customer updated = service().updateCustomer(employee, desired);

        assertEquals("Updated Name", updated.getName());
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("Updated Name",
                harness.customerRepository.findByIdentification(desired).get().getName());
        assertEquals(1, harness.operationCount(OperationType.CUSTOMER_UPDATE));
        assertEquals(1, harness.auditCount(OperationType.CUSTOMER_UPDATE));
    }

    @Test
    void naturalCustomerUpdatesOwnInformation() {
        Customer persisted = CustomerTestSupport.adultCustomer("4002");
        harness.customerRepository.save(persisted);
        User requesting = CustomerTestSupport.customerUser(2, persisted);
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("4002");
        desired.setPhoneNumber("3111111111");

        Customer updated = service().updateCustomer(requesting, desired);

        assertEquals("3111111111", updated.getPhoneNumber());
        assertEquals(1, harness.operationCount(OperationType.CUSTOMER_UPDATE));
    }

    @Test
    void failsWhenIdentificationDoesNotMatchAnyCustomer() {
        Customer owned = CustomerTestSupport.adultCustomer("4003");
        harness.customerRepository.save(owned);
        User requesting = CustomerTestSupport.customerUser(3, owned);
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("4004");
        User employee = CustomerTestSupport.employeeUser(3);

        assertThrows(CustomerNotFoundException.class,
                () -> service().updateCustomer(employee, desired));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_UPDATE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_UPDATE));
    }

    @Test
    void failsWhenIdentificationChangeIsAttemptedOnExistingCustomer() {
        Customer owned = CustomerTestSupport.adultCustomer("4006");
        harness.customerRepository.save(owned);
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("4007"));
        User requesting = CustomerTestSupport.customerUser(5, owned);
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("4007");

        assertThrows(UnauthorizedCustomerOperationException.class,
                () -> service().updateCustomer(requesting, desired));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_UPDATE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_UPDATE));
    }

    @Test
    void updateDoesNotChangeStatus() {
        NaturalCustomer persisted = CustomerTestSupport.adultCustomer("4008");
        harness.customerRepository.save(persisted);
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("4008");
        desired.setStatus(CustomerStatus.BLOCKED);
        User employee = CustomerTestSupport.employeeUser(6);

        Customer updated = service().updateCustomer(employee, desired);

        assertEquals(CustomerStatus.ACTIVE, updated.getStatus());
    }
}