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
import application.domain.valueobjects.OperationType;

class ConsultCustomerServiceTest {

    private CustomerServiceHarness harness;

    @BeforeEach
    void setUp() {
        harness = new CustomerServiceHarness();
    }

    private ConsultCustomerService service() {
        return new ConsultCustomerService(
                harness.customerRepository,
                harness.authorizeCustomerOperation);
    }

    @Test
    void employeeConsultsExistingCustomer() {
        Customer persisted = CustomerTestSupport.adultCustomer("3001");
        harness.customerRepository.save(persisted);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("3001");
        User employee = CustomerTestSupport.employeeUser(1);

        Customer found = service().consultCustomer(employee, request);

        assertEquals("3001", found.getIdentification());
    }

    @Test
    void naturalCustomerConsultsOwnCustomer() {
        Customer persisted = CustomerTestSupport.adultCustomer("3002");
        harness.customerRepository.save(persisted);
        User requesting = CustomerTestSupport.customerUser(2, persisted);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("3002");

        Customer found = service().consultCustomer(requesting, request);

        assertEquals("3002", found.getIdentification());
    }

    @Test
    void naturalCustomerCannotConsultOtherCustomer() {
        Customer owned = CustomerTestSupport.adultCustomer("3003");
        harness.customerRepository.save(owned);
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("3004"));
        User requesting = CustomerTestSupport.customerUser(3, owned);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("3004");

        assertThrows(UnauthorizedCustomerOperationException.class,
                () -> service().consultCustomer(requesting, request));
    }

    @Test
    void failsWhenCustomerDoesNotExist() {
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("3005");
        User employee = CustomerTestSupport.employeeUser(4);

        assertThrows(CustomerNotFoundException.class,
                () -> service().consultCustomer(employee, request));
    }

    @Test
    void failsWhenCustomerIsNull() {
        User employee = CustomerTestSupport.employeeUser(5);

        assertThrows(InvalidCustomerException.class,
                () -> service().consultCustomer(employee, null));
    }

    @Test
    void consultationDoesNotRegisterOperationOrAudit() {
        Customer persisted = CustomerTestSupport.adultCustomer("3006");
        harness.customerRepository.save(persisted);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("3006");
        User employee = CustomerTestSupport.employeeUser(6);

        service().consultCustomer(employee, request);

        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_UPDATE));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }
}