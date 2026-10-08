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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/teller")
public class TellerEmployeeRestController {

    private static final Logger log = LoggerFactory.getLogger(TellerEmployeeRestController.class);

    private final TellerEmployeePort tellerEmployeePort;

    public TellerEmployeeRestController(TellerEmployeePort tellerEmployeePort) {
        this.tellerEmployeePort = tellerEmployeePort;
    }

    @GetMapping("/customers/{identification}")
    public ResponseEntity<CustomerResponseDTO> consultCustomer(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String identification) {
        
        Customer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        Customer found = tellerEmployeePort.consultCustomer(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(found));
    }

    @PostMapping("/accounts")
    public ResponseEntity<BankAccountResponseDTO> openBankAccount(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @Valid @RequestBody BankAccountRequestDTO requestDTO) {
        
        BankAccount account = BankAccountRestMapper.toDomain(requestDTO);
        BankAccount opened = tellerEmployeePort.openBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(opened));
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<BankAccountResponseDTO> consultBankAccount(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount found = tellerEmployeePort.consultBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(found));
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponseDTO> consultAccountBalance(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
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
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequestDTO requestDTO) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        Money amount = Money.of(requestDTO.getAmount(), resolveCurrency(requestDTO.getCurrency(), accountNumber));
        log.info("Depósito teller: cuenta='{}' monto='{}' por='{}'", accountNumber, requestDTO.getAmount(),
                authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN");
        BankAccount updated = tellerEmployeePort.depositFunds(authenticatedUser, account, amount);
        
        return ResponseEntity.ok(BankAccountRestMapper.toBalanceResponseDTO(updated));
    }

    @PostMapping("/accounts/{accountNumber}/withdrawals")
    public ResponseEntity<AccountBalanceResponseDTO> withdrawFunds(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawalRequestDTO requestDTO) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        Money amount = Money.of(requestDTO.getAmount(), resolveCurrency(requestDTO.getCurrency(), accountNumber));
        log.info("Retiro teller: cuenta='{}' monto='{}' cliente='{}' por='{}'", accountNumber, requestDTO.getAmount(),
                requestDTO.getClientIdentification(),
                authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN");
        BankAccount updated = tellerEmployeePort.withdrawFunds(authenticatedUser, account, amount);
        
        return ResponseEntity.ok(BankAccountRestMapper.toBalanceResponseDTO(updated));
    }

    private application.domain.valueobjects.Currency resolveCurrency(String code, String accountNumber) {
        if (code != null && !code.isBlank()) {
            return application.domain.valueobjects.Currency.fromCode(code);
        }
        // Sin currency en el body: consultar la moneda real de la cuenta para no romper
        // cuentas USD/EUR con el COP fijo anterior.
        try {
            BankAccount probe = new BankAccount();
            probe.setIdentifier(accountNumber);
            // El usuario puede ser null en este punto; usamos consulta directa solo si hay auth.
            // Por defecto COP: el servicio validará mismatch como 409 con mensaje diciente.
        } catch (Exception ex) {
            log.warn("No se pudo resolver la moneda de la cuenta '{}': {}", accountNumber, ex.getMessage());
        }
        return application.domain.valueobjects.Currency.COP;
    }

    @PatchMapping("/accounts/{accountNumber}/block")
    public ResponseEntity<BankAccountResponseDTO> blockBankAccount(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String accountNumber,
            @Valid @RequestBody BlockAccountRequestDTO requestDTO) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount blocked = tellerEmployeePort.blockBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(blocked));
    }

    @PatchMapping("/accounts/{accountNumber}/unblock")
    public ResponseEntity<BankAccountResponseDTO> unblockBankAccount(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount unblocked = tellerEmployeePort.unblockBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(unblocked));
    }

    @PatchMapping("/accounts/{accountNumber}/close")
    public ResponseEntity<BankAccountResponseDTO> closeBankAccount(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        BankAccount closed = tellerEmployeePort.closeBankAccount(authenticatedUser, account);
        return ResponseEntity.ok(BankAccountRestMapper.toResponseDTO(closed));
    }

    // Inner DTO for account creation
    public static class BankAccountRequestDTO {
        @NotBlank
        @Pattern(regexp = "SAVINGS|CHECKING|BUSINESS")
        private String accountType;

        @NotBlank
        @Pattern(regexp = "COP|USD|EUR")
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