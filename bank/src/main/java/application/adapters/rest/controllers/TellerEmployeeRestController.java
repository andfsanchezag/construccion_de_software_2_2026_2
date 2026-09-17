package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.DepositRequestDTO;
import application.adapters.rest.dtos.requests.WithdrawalRequestDTO;
import application.adapters.rest.dtos.requests.BlockAccountRequestDTO;
import application.adapters.rest.dtos.responses.CustomerResponseDTO;
import application.adapters.rest.dtos.responses.BankAccountResponseDTO;
import application.adapters.rest.dtos.responses.AccountBalanceResponseDTO;
import application.adapters.rest.mappers.CustomerRestMapper;
import application.adapters.rest.mappers.BankAccountRestMapper;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.valueobjects.Money;
import application.domain.ports.in.TellerEmployeePort;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/teller")
public class TellerEmployeeRestController {

    private final TellerEmployeePort tellerEmployeePort;

    public TellerEmployeeRestController(TellerEmployeePort tellerEmployeePort) {
        this.tellerEmployeePort = tellerEmployeePort;
    }

    @GetMapping("/customers/{identification}")
    public ResponseEntity<CustomerResponseDTO> consultCustomer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String identification) {
        
        Customer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        Customer found = tellerEmployeePort.consultCustomer(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(found));
    }

    @PostMapping("/accounts")
    public ResponseEntity<BankAccountResponseDTO> openBankAccount(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestBody BankAccountRequestDTO requestDTO) {
        
        BankAccount account = BankAccountRestMapper.toDomain(requestDTO);
        BankAccount opened = tellerEmployeePort.openBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(opened));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<BankAccountResponseDTO> consultBankAccount(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount found = tellerEmployeePort.consultBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(found));
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponseDTO> consultAccountBalance(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        Money balance = tellerEmployeePort.consultAccountBalance(authenticatedUser, account);
        
        BankAccount found = new BankAccount();
        found.setIdentifier(accountNumber);
        found.setCurrentBalance(balance != null ? balance.getAmount() : null);
        found.setCurrency(balance != null ? balance.getCurrency() : null);
        
        return ResponseEntity.ok(BankAccountRestMapper.toBalanceResponseDTO(found));
    }

    @PostMapping("/accounts/{accountNumber}/deposits")
    public ResponseEntity<AccountBalanceResponseDTO> depositFunds(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequestDTO requestDTO) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        Money amount = Money.of(requestDTO.getAmount(), application.domain.valueobjects.Currency.COP);
        BankAccount updated = tellerEmployeePort.depositFunds(authenticatedUser, account, amount);
        
        return ResponseEntity.ok(BankAccountRestMapper.toBalanceResponseDTO(updated));
    }

    @PostMapping("/accounts/{accountNumber}/withdrawals")
    public ResponseEntity<AccountBalanceResponseDTO> withdrawFunds(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawalRequestDTO requestDTO) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        Money amount = Money.of(requestDTO.getAmount(), application.domain.valueobjects.Currency.COP);
        BankAccount updated = tellerEmployeePort.withdrawFunds(authenticatedUser, account, amount);
        
        return ResponseEntity.ok(BankAccountRestMapper.toBalanceResponseDTO(updated));
    }

    @PatchMapping("/accounts/{accountNumber}/block")
    public ResponseEntity<BankAccountResponseDTO> blockBankAccount(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber,
            @Valid @RequestBody BlockAccountRequestDTO requestDTO) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount blocked = tellerEmployeePort.blockBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(blocked));
    }

    @PatchMapping("/accounts/{accountNumber}/unblock")
    public ResponseEntity<BankAccountResponseDTO> unblockBankAccount(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount unblocked = tellerEmployeePort.unblockBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(unblocked));
    }

    @PatchMapping("/accounts/{accountNumber}/close")
    public ResponseEntity<BankAccountResponseDTO> closeBankAccount(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount closed = tellerEmployeePort.closeBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(closed));
    }

    // Inner DTO for account creation
    public static class BankAccountRequestDTO {
        private String accountType;
        private String currency;
        private BigDecimal initialBalance;
        private String ownerIdentification;

        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public BigDecimal getInitialBalance() { return initialBalance; }
        public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
        public String getOwnerIdentification() { return ownerIdentification; }
        public void setOwnerIdentification(String ownerIdentification) { this.ownerIdentification = ownerIdentification; }
    }
}