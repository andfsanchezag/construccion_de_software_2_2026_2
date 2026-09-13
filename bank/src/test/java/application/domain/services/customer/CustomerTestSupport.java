package application.domain.services.customer;

import java.math.BigDecimal;
import java.time.LocalDate;

import application.domain.models.BankAccount;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.NaturalCustomer;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.LoanStatus;
import application.domain.valueobjects.LoanType;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.TransferStatus;
import application.domain.valueobjects.UserStatus;

final class CustomerTestSupport {

    private CustomerTestSupport() {
    }

    static NaturalCustomer naturalCustomer(String identification, LocalDate birthDate, CustomerStatus status) {
        NaturalCustomer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        customer.setName("Customer " + identification);
        customer.setEmail("customer" + identification + "@example.com");
        customer.setPhoneNumber("3000000000");
        customer.setAddress("Address " + identification);
        customer.setRole(SystemRole.NATURAL_CUSTOMER);
        customer.setBirthDate(birthDate);
        customer.setStatus(status);
        return customer;
    }

    static NaturalCustomer adultCustomer(String identification) {
        return naturalCustomer(identification, LocalDate.now().minusYears(30), CustomerStatus.ACTIVE);
    }

    static NaturalCustomer registrationCandidate(String identification) {
        return naturalCustomer(identification, LocalDate.now().minusYears(30), null);
    }

    static BusinessCustomer businessCustomer(String identification, NaturalCustomer legalRepresentative) {
        BusinessCustomer customer = new BusinessCustomer();
        customer.setIdentification(identification);
        customer.setName("Business " + identification);
        customer.setEmail("business" + identification + "@example.com");
        customer.setPhoneNumber("3010000000");
        customer.setAddress("Business address " + identification);
        customer.setRole(SystemRole.BUSINESS_CUSTOMER);
        customer.setLegalRepresentative(legalRepresentative);
        return customer;
    }

    static User employeeUser(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("employee" + userId);
        user.setRole(SystemRole.TELLER_EMPLOYEE);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    static User analystUser(Integer userId) {
        User user = employeeUser(userId);
        user.setRole(SystemRole.INTERNAL_ANALYST);
        return user;
    }

    static User customerUser(Integer userId, Customer customer) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("customer" + userId);
        user.setRole(SystemRole.NATURAL_CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCustomer(customer);
        user.setIdentification(customer.getIdentification());
        return user;
    }

    static User inactiveUser(Integer userId, SystemRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("inactive" + userId);
        user.setRole(role);
        user.setStatus(UserStatus.INACTIVE);
        return user;
    }

    static BankAccount account(String identifier, Customer owner) {
        BankAccount account = new BankAccount();
        account.setIdentifier(identifier);
        account.setOwner(owner);
        account.setAccountType(AccountType.SAVINGS);
        account.setCurrency(Currency.COP);
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setAccountStatus(AccountStatus.ACTIVE);
        return account;
    }

    static Loan loan(String identifier, Customer applicant) {
        Loan loan = new Loan();
        loan.setIdentifier(identifier);
        loan.setApplicant(applicant);
        loan.setLoanType(LoanType.PERSONAL);
        loan.setRequestedAmount(new BigDecimal("5000.00"));
        loan.setTermInMonths(12);
        loan.setCurrency(Currency.COP);
        loan.submitForReview();
        return loan;
    }

    static Transfer transfer(String identifier, BankAccount source, BankAccount destination) {
        Transfer transfer = new Transfer();
        transfer.setIdentifier(identifier);
        transfer.setSourceAccount(source);
        transfer.setDestinationAccount(destination);
        transfer.setAmount(new BigDecimal("100.00"));
        transfer.markWaitingForApproval();
        return transfer;
    }
}