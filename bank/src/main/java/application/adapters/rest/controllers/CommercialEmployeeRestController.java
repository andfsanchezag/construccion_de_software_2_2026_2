package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CommercialRequestLoanRequestDTO;
import application.adapters.rest.dtos.responses.CustomerResponseDTO;
import application.adapters.rest.dtos.responses.CustomerProductsResponseDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.adapters.rest.dtos.responses.BankAccountResponseDTO;
import application.adapters.rest.mappers.CustomerRestMapper;
import application.adapters.rest.mappers.CustomerProductsRestMapper;
import application.adapters.rest.mappers.LoanRestMapper;
import application.adapters.rest.mappers.BankAccountRestMapper;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.Loan;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.ports.in.CommercialEmployeePort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/commercial")
public class CommercialEmployeeRestController {

    private final CommercialEmployeePort commercialEmployeePort;

    public CommercialEmployeeRestController(CommercialEmployeePort commercialEmployeePort) {
        this.commercialEmployeePort = commercialEmployeePort;
    }

    @GetMapping("/customers/{identification}")
    public ResponseEntity<CustomerResponseDTO> consultCustomer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String identification) {
        
        Customer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        Customer found = commercialEmployeePort.consultCustomer(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(found));
    }

    @PutMapping("/customers/{identification}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String identification,
            @RequestBody CustomerUpdateRequestDTO requestDTO) {
        
        Customer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        customer.setEmail(requestDTO.getEmail());
        customer.setPhoneNumber(requestDTO.getPhoneNumber());
        customer.setAddress(requestDTO.getAddress());
        
        Customer updated = commercialEmployeePort.updateCustomer(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(updated));
    }

    @GetMapping("/customers/{identification}/products")
    public ResponseEntity<CustomerProductsResponseDTO> consultCustomerProducts(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String identification) {
        
        Customer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        application.domain.models.CustomerProducts products = commercialEmployeePort.consultCustomerProducts(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerProductsRestMapper.toResponseDTO(products));
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponseDTO> requestLoanOnBehalfOfCustomer(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody CommercialRequestLoanRequestDTO requestDTO) {
        
        Customer customer = new NaturalCustomer();
        customer.setIdentification(requestDTO.getCustomerIdentification());
        
        Loan loan = LoanRestMapper.toDomain(requestDTO);
        Loan requested = commercialEmployeePort.requestLoanOnBehalfOfCustomer(authenticatedUser, customer, loan);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanRestMapper.toResponseDTO(requested));
    }

    @GetMapping("/loans/{loanId}")
    public ResponseEntity<LoanResponseDTO> consultLoanStatus(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        Loan found = commercialEmployeePort.consultLoanStatus(authenticatedUser, loan);
        return ResponseEntity.ok(LoanRestMapper.toResponseDTO(found));
    }

    @PostMapping("/accounts")
    public ResponseEntity<BankAccountResponseDTO> openBankAccount(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody BankAccountRequestDTO requestDTO) {
        
        BankAccount account = toDomain(requestDTO);
        BankAccount opened = commercialEmployeePort.openBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(opened));
    }

    private BankAccount toDomain(BankAccountRequestDTO dto) {
        BankAccount account = new BankAccount();
        if (dto.getAccountType() != null) {
            account.setAccountType(mapAccountType(dto.getAccountType()));
        }
        if (dto.getCurrency() != null) {
            account.setCurrency(mapCurrency(dto.getCurrency()));
        }
        if (dto.getInitialBalance() != null) {
            account.setCurrentBalance(dto.getInitialBalance());
        }
        if (dto.getOwnerIdentification() != null) {
            NaturalCustomer owner = new NaturalCustomer();
            owner.setIdentification(dto.getOwnerIdentification());
            account.setOwner(owner);
        }
        return account;
    }

    private application.domain.valueobjects.AccountType mapAccountType(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "SAVINGS":
                return application.domain.valueobjects.AccountType.SAVINGS;
            case "CHECKING":
                return application.domain.valueobjects.AccountType.CHECKING;
            case "BUSINESS":
                return application.domain.valueobjects.AccountType.BUSINESS;
            default:
                return null;
        }
    }

    private application.domain.valueobjects.Currency mapCurrency(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "COP":
                return application.domain.valueobjects.Currency.COP;
            case "USD":
                return application.domain.valueobjects.Currency.USD;
            case "EUR":
                return application.domain.valueobjects.Currency.EUR;
            default:
                return null;
        }
    }

    // Inner DTOs
    public static class CustomerUpdateRequestDTO {
        private String email;
        private String phoneNumber;
        private String address;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class BankAccountRequestDTO {
        private String accountType;
        private String currency;
        private java.math.BigDecimal initialBalance;
        private String ownerIdentification;

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public java.math.BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(java.math.BigDecimal initialBalance) { this.initialBalance = initialBalance; }
        public String getOwnerIdentification() { return ownerIdentification; }
        public void setOwnerIdentification(String ownerIdentification) { this.ownerIdentification = ownerIdentification; }
    }
}