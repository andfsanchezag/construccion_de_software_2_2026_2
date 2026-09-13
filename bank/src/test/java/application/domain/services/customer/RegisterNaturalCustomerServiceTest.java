package application.domain.services.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.CustomerAlreadyExistsException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;

class RegisterNaturalCustomerServiceTest {

    private CustomerServiceHarness harness;

    @BeforeEach
    void setUp() {
        harness = new CustomerServiceHarness();
    }

    private RegisterNaturalCustomerService service() {
        return new RegisterNaturalCustomerService(
                harness.customerRepository,
                harness.authorizeCustomerRegistration,
                harness.registerOperationAndAudit);
    }

    @Test
    void registersAdultCustomerWithInitialActiveStatus() {
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("1001");
        User employee = CustomerTestSupport.employeeUser(1);

        NaturalCustomer saved = service().registerNaturalCustomer(employee, customer);

        assertEquals(CustomerStatus.ACTIVE, saved.getStatus());
        assertTrue(harness.customerRepository.findByIdentification(saved).isPresent());
        assertEquals(1, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(1, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenIdentificationAlreadyExists() {
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("1002"));
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("1002");
        User employee = CustomerTestSupport.employeeUser(2);

        assertThrows(CustomerAlreadyExistsException.class,
                () -> service().registerNaturalCustomer(employee, customer));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenCustomerIsUnderage() {
        NaturalCustomer customer = CustomerTestSupport.naturalCustomer(
                "1003", LocalDate.now().minusYears(17), null);
        User employee = CustomerTestSupport.employeeUser(3);

        assertThrows(InvalidCustomerException.class,
                () -> service().registerNaturalCustomer(employee, customer));
        assertTrue(harness.customerRepository.findAll().isEmpty());
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenActorIsUnauthorized() {
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("1004");
        User analyst = CustomerTestSupport.analystUser(4);

        assertThrows(UnauthorizedCustomerOperationException.class,
                () -> service().registerNaturalCustomer(analyst, customer));
        assertTrue(harness.customerRepository.findAll().isEmpty());
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenActorIsInactive() {
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("1005");
        User inactive = CustomerTestSupport.inactiveUser(5, SystemRole.TELLER_EMPLOYEE);

        assertThrows(UnauthorizedOperationException.class,
                () -> service().registerNaturalCustomer(inactive, customer));
        assertTrue(harness.customerRepository.findAll().isEmpty());
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenCustomerIsNull() {
        User employee = CustomerTestSupport.employeeUser(6);

        assertThrows(InvalidCustomerException.class,
                () -> service().registerNaturalCustomer(employee, null));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void naturalCustomerMayRegisterItsOwnCustomer() {
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("1006");
        User requesting = CustomerTestSupport.customerUser(7, customer);

        NaturalCustomer saved = service().registerNaturalCustomer(requesting, customer);

        assertEquals(CustomerStatus.ACTIVE, saved.getStatus());
        assertEquals(1, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(1, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }
}