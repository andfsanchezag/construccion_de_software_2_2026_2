package application.domain.services.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.CustomerAlreadyExistsException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.InvalidLegalRepresentativeException;
import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.BusinessCustomer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.OperationType;

class RegisterBusinessCustomerServiceTest {

    private CustomerServiceHarness harness;

    @BeforeEach
    void setUp() {
        harness = new CustomerServiceHarness();
    }

    private RegisterBusinessCustomerService service() {
        return new RegisterBusinessCustomerService(
                harness.customerRepository,
                harness.authorizeCustomerRegistration,
                harness.registerOperationAndAudit);
    }

    @Test
    void registersBusinessCustomerWithPersistedLegalRepresentative() {
        NaturalCustomer legalRepresentative = CustomerTestSupport.adultCustomer("2001");
        harness.customerRepository.save(legalRepresentative);
        NaturalCustomer reference = new NaturalCustomer();
        reference.setIdentification("2001");
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("2002", reference);
        User employee = CustomerTestSupport.employeeUser(1);

        BusinessCustomer saved = service().registerBusinessCustomer(employee, customer);

        assertEquals(CustomerStatus.ACTIVE, saved.getStatus());
        assertEquals("2001", saved.getLegalRepresentative().getIdentification());
        assertEquals(CustomerStatus.ACTIVE, saved.getLegalRepresentative().getStatus());
        assertEquals(1, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(1, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenLegalRepresentativeIsMissing() {
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("2003", null);
        User employee = CustomerTestSupport.employeeUser(2);

        assertThrows(InvalidLegalRepresentativeException.class,
                () -> service().registerBusinessCustomer(employee, customer));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenLegalRepresentativeDoesNotExist() {
        NaturalCustomer reference = new NaturalCustomer();
        reference.setIdentification("2004");
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("2005", reference);
        User employee = CustomerTestSupport.employeeUser(3);

        assertThrows(InvalidLegalRepresentativeException.class,
                () -> service().registerBusinessCustomer(employee, customer));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenLegalRepresentativeIsNotActive() {
        NaturalCustomer legalRepresentative = CustomerTestSupport.naturalCustomer(
                "2006", LocalDate.now().minusYears(30), CustomerStatus.BLOCKED);
        harness.customerRepository.save(legalRepresentative);
        NaturalCustomer reference = new NaturalCustomer();
        reference.setIdentification("2006");
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("2007", reference);
        User employee = CustomerTestSupport.employeeUser(4);

        assertThrows(InvalidLegalRepresentativeException.class,
                () -> service().registerBusinessCustomer(employee, customer));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenBusinessIdentificationAlreadyExists() {
        NaturalCustomer legalRepresentative = CustomerTestSupport.adultCustomer("2008");
        harness.customerRepository.save(legalRepresentative);
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("2009"));
        NaturalCustomer reference = new NaturalCustomer();
        reference.setIdentification("2008");
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("2009", reference);
        User employee = CustomerTestSupport.employeeUser(5);

        assertThrows(CustomerAlreadyExistsException.class,
                () -> service().registerBusinessCustomer(employee, customer));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenActorIsUnauthorized() {
        NaturalCustomer legalRepresentative = CustomerTestSupport.adultCustomer("2010");
        harness.customerRepository.save(legalRepresentative);
        NaturalCustomer reference = new NaturalCustomer();
        reference.setIdentification("2010");
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("2011", reference);
        User analyst = CustomerTestSupport.analystUser(6);

        assertThrows(UnauthorizedCustomerOperationException.class,
                () -> service().registerBusinessCustomer(analyst, customer));
        assertTrue(harness.customerRepository.findByIdentification(customer).isEmpty());
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }

    @Test
    void failsWhenBusinessCustomerIsNull() {
        User employee = CustomerTestSupport.employeeUser(7);

        assertThrows(InvalidCustomerException.class,
                () -> service().registerBusinessCustomer(employee, null));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }
}