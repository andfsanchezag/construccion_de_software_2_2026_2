package application.domain.services.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerStatusException;
import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.UserStatus;

class ChangeCustomerStatusServiceTest {

    private CustomerServiceHarness harness;

    @BeforeEach
    void setUp() {
        harness = new CustomerServiceHarness();
    }

    private ChangeCustomerStatusService service() {
        return new ChangeCustomerStatusService(
                harness.customerRepository,
                harness.authorizeChangeCustomerStatus,
                harness.registerOperationAndAudit);
    }

    @Test
    void employeeChangesActiveToBlocked() {
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("5001"));
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5001");
        request.setStatus(CustomerStatus.BLOCKED);
        User employee = CustomerTestSupport.employeeUser(1);

        Customer updated = service().changeCustomerStatus(employee, request);

        assertEquals(CustomerStatus.BLOCKED, updated.getStatus());
        assertEquals(CustomerStatus.BLOCKED,
                harness.customerRepository.findByIdentification(request).get().getStatus());
        assertEquals(1, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(1, harness.auditCount(OperationType.CUSTOMER_STATUS_CHANGE));
    }

    @Test
    void employeeChangesBlockedToActive() {
        NaturalCustomer persisted = CustomerTestSupport.adultCustomer("5002");
        persisted.setStatus(CustomerStatus.BLOCKED);
        harness.customerRepository.save(persisted);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5002");
        request.setStatus(CustomerStatus.ACTIVE);
        User employee = CustomerTestSupport.employeeUser(2);

        Customer updated = service().changeCustomerStatus(employee, request);

        assertEquals(CustomerStatus.ACTIVE, updated.getStatus());
    }

    @Test
    void failsWhenTransitionIsNotAllowed() {
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("5003"));
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5003");
        request.setStatus(CustomerStatus.ACTIVE);
        User employee = CustomerTestSupport.employeeUser(3);

        assertThrows(InvalidCustomerStatusException.class,
                () -> service().changeCustomerStatus(employee, request));
        assertEquals(CustomerStatus.ACTIVE,
                harness.customerRepository.findByIdentification(request).get().getStatus());
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_STATUS_CHANGE));
    }

    @Test
    void failsWhenTargetStatusIsNotProvided() {
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("5004"));
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5004");
        User employee = CustomerTestSupport.employeeUser(4);

        assertThrows(InvalidCustomerStatusException.class,
                () -> service().changeCustomerStatus(employee, request));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_STATUS_CHANGE));
    }

    @Test
    void failsWhenActorIsNotABankingEmployee() {
        Customer persisted = CustomerTestSupport.adultCustomer("5005");
        harness.customerRepository.save(persisted);
        User requesting = CustomerTestSupport.customerUser(5, persisted);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5005");
        request.setStatus(CustomerStatus.BLOCKED);

        assertThrows(UnauthorizedCustomerOperationException.class,
                () -> service().changeCustomerStatus(requesting, request));
        assertEquals(CustomerStatus.ACTIVE,
                harness.customerRepository.findByIdentification(request).get().getStatus());
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_STATUS_CHANGE));
    }

    @Test
    void failsWhenCustomerDoesNotExist() {
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5006");
        request.setStatus(CustomerStatus.BLOCKED);
        User employee = CustomerTestSupport.employeeUser(6);

        assertThrows(CustomerNotFoundException.class,
                () -> service().changeCustomerStatus(employee, request));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_STATUS_CHANGE));
    }

    @Test
    void customerStatusChangeIsIndependentFromUserStatus() {
        Customer persisted = CustomerTestSupport.adultCustomer("5007");
        harness.customerRepository.save(persisted);
        User requesting = CustomerTestSupport.customerUser(7, persisted);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("5007");
        request.setStatus(CustomerStatus.BLOCKED);
        User employee = CustomerTestSupport.employeeUser(8);

        service().changeCustomerStatus(employee, request);

        assertEquals(UserStatus.ACTIVE, employee.getStatus());
        assertEquals(UserStatus.ACTIVE, requesting.getStatus());
    }
}