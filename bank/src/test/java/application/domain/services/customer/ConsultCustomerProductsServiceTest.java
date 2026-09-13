package application.domain.services.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.UnauthorizedCustomerOperationException;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.NaturalCustomer;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.OperationType;

class ConsultCustomerProductsServiceTest {

    private CustomerServiceHarness harness;

    @BeforeEach
    void setUp() {
        harness = new CustomerServiceHarness();
    }

    private ConsultCustomerProductsService service() {
        return new ConsultCustomerProductsService(
                harness.customerRepository,
                harness.bankAccountRepository,
                harness.loanRepository,
                harness.transferRepository,
                harness.authorizeCustomerOperation);
    }

    @Test
    void employeeConsultsCustomerProducts() {
        Customer customer = CustomerTestSupport.adultCustomer("6001");
        harness.customerRepository.save(customer);
        BankAccount account = CustomerTestSupport.account("BA-1", customer);
        BankAccount otherAccount = CustomerTestSupport.account("BA-2", customer);
        harness.bankAccountRepository.save(account);
        harness.bankAccountRepository.save(otherAccount);
        harness.loanRepository.seed(CustomerTestSupport.loan("LO-1", customer));
        harness.transferRepository.seed(CustomerTestSupport.transfer("TR-1", account, otherAccount));
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6001");
        User employee = CustomerTestSupport.employeeUser(1);

        CustomerProducts products = service().consultCustomerProducts(employee, request);

        assertEquals(2, products.getAccounts().size());
        assertEquals(1, products.getLoans().size());
        assertEquals(1, products.getTransfers().size());
    }

    @Test
    void customerWithNoProductsGetsEmptyCollections() {
        Customer customer = CustomerTestSupport.adultCustomer("6002");
        harness.customerRepository.save(customer);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6002");
        User employee = CustomerTestSupport.employeeUser(2);

        CustomerProducts products = service().consultCustomerProducts(employee, request);

        assertTrue(products.getAccounts().isEmpty());
        assertTrue(products.getLoans().isEmpty());
        assertTrue(products.getTransfers().isEmpty());
    }

    @Test
    void incomingTransferIsIncluded() {
        Customer customer = CustomerTestSupport.adultCustomer("6003");
        Customer other = CustomerTestSupport.adultCustomer("6004");
        harness.customerRepository.save(customer);
        harness.customerRepository.save(other);
        BankAccount account = CustomerTestSupport.account("BA-3", customer);
        BankAccount foreignAccount = CustomerTestSupport.account("BA-4", other);
        harness.bankAccountRepository.save(account);
        harness.bankAccountRepository.save(foreignAccount);
        harness.transferRepository.seed(CustomerTestSupport.transfer("TR-2", foreignAccount, account));
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6003");
        User employee = CustomerTestSupport.employeeUser(3);

        CustomerProducts products = service().consultCustomerProducts(employee, request);

        assertEquals(1, products.getTransfers().size());
    }

    @Test
    void naturalCustomerConsultsOwnProducts() {
        Customer customer = CustomerTestSupport.adultCustomer("6005");
        harness.customerRepository.save(customer);
        harness.bankAccountRepository.save(CustomerTestSupport.account("BA-5", customer));
        User requesting = CustomerTestSupport.customerUser(4, customer);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6005");

        CustomerProducts products = service().consultCustomerProducts(requesting, request);

        assertEquals(1, products.getAccounts().size());
    }

    @Test
    void naturalCustomerCannotConsultOtherProducts() {
        Customer owned = CustomerTestSupport.adultCustomer("6006");
        harness.customerRepository.save(owned);
        harness.customerRepository.save(CustomerTestSupport.adultCustomer("6007"));
        User requesting = CustomerTestSupport.customerUser(5, owned);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6007");

        assertThrows(UnauthorizedCustomerOperationException.class,
                () -> service().consultCustomerProducts(requesting, request));
    }

    @Test
    void failsWhenCustomerDoesNotExist() {
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6008");
        User employee = CustomerTestSupport.employeeUser(6);

        assertThrows(CustomerNotFoundException.class,
                () -> service().consultCustomerProducts(employee, request));
    }

    @Test
    void failsWhenCustomerIsNull() {
        User employee = CustomerTestSupport.employeeUser(7);

        assertThrows(InvalidCustomerException.class,
                () -> service().consultCustomerProducts(employee, null));
    }

    @Test
    void consultationDoesNotRegisterOperationOrAudit() {
        Customer customer = CustomerTestSupport.adultCustomer("6009");
        harness.customerRepository.save(customer);
        NaturalCustomer request = new NaturalCustomer();
        request.setIdentification("6009");
        User employee = CustomerTestSupport.employeeUser(8);

        service().consultCustomerProducts(employee, request);

        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_REGISTRATION));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_UPDATE));
        assertEquals(0, harness.operationCount(OperationType.CUSTOMER_STATUS_CHANGE));
        assertEquals(0, harness.auditCount(OperationType.CUSTOMER_REGISTRATION));
    }
}