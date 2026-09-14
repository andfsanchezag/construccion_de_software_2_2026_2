package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.RequestLoanRequestDTO;
import application.adapters.rest.dtos.requests.LoanPaymentRequestDTO;
import application.adapters.rest.dtos.requests.CreateTransferRequestDTO;
import application.adapters.rest.dtos.requests.UpdateCustomerProfileRequestDTO;
import application.adapters.rest.dtos.responses.CustomerResponseDTO;
import application.adapters.rest.dtos.responses.CustomerProductsResponseDTO;
import application.adapters.rest.dtos.responses.BankAccountResponseDTO;
import application.adapters.rest.dtos.responses.AccountBalanceResponseDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.adapters.rest.dtos.responses.LoanPaymentResponseDTO;
import application.adapters.rest.dtos.responses.TransferResponseDTO;
import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.adapters.rest.mappers.CustomerRestMapper;
import application.adapters.rest.mappers.LoanRestMapper;
import application.adapters.rest.mappers.TransferRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.adapters.rest.mappers.CustomerProductsRestMapper;
import application.adapters.rest.mappers.BankAccountRestMapper;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Transfer;
import application.domain.models.Operation;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.valueobjects.Money;
import application.domain.ports.in.NaturalCustomerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/natural-customer")
public class NaturalCustomerRestController {

    private final NaturalCustomerPort naturalCustomerPort;

    public NaturalCustomerRestController(NaturalCustomerPort naturalCustomerPort) {
        this.naturalCustomerPort = naturalCustomerPort;
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> consultMyProfile(@AuthenticationPrincipal User authenticatedUser) {
        Customer customer = naturalCustomerPort.consultMyProfile(authenticatedUser);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(customer));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> updateMyProfile(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody UpdateCustomerProfileRequestDTO requestDTO) {
        
        Customer customer = authenticatedUser.getCustomer();
        CustomerRestMapper.updateDomainFromDTO(requestDTO, customer);
        Customer updated = naturalCustomerPort.updateMyProfile(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(updated));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponseDTO>> consultMyAccounts(@AuthenticationPrincipal User authenticatedUser) {
        List<BankAccount> accounts = naturalCustomerPort.consultMyAccounts(authenticatedUser);
        return ResponseEntity.ok(accounts.stream()
                .map(BankAccountRestMapper::toResponseDTO)
                .toList());
    }

    @GetMapping("/products")
    public ResponseEntity<CustomerProductsResponseDTO> consultMyProducts(@AuthenticationPrincipal User authenticatedUser) {
        application.domain.models.CustomerProducts products = naturalCustomerPort.consultMyProducts(authenticatedUser);
        return ResponseEntity.ok(CustomerProductsRestMapper.toResponseDTO(products));
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponseDTO> consultAccountBalance(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String accountNumber) {
        
        BankAccount account = new BankAccount();
        account.setIdentifier(accountNumber);
        Money balance = naturalCustomerPort.consultAccountBalance(authenticatedUser, account);
        
        BankAccount found = new BankAccount();
        found.setIdentifier(accountNumber);
        found.setCurrentBalance(balance != null ? balance.getAmount() : null);
        found.setCurrency(balance != null ? balance.getCurrency() : null);
        
        return ResponseEntity.ok(BankAccountRestMapper.toBalanceResponseDTO(found));
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody RequestLoanRequestDTO requestDTO) {
        
        Loan loan = LoanRestMapper.toDomain(requestDTO);
        Loan requested = naturalCustomerPort.requestLoan(authenticatedUser, loan);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanRestMapper.toResponseDTO(requested));
    }

    @GetMapping("/loans/{loanId}")
    public ResponseEntity<LoanResponseDTO> consultLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        Loan found = naturalCustomerPort.consultLoan(authenticatedUser, loan);
        return ResponseEntity.ok(LoanRestMapper.toResponseDTO(found));
    }

    @PostMapping("/loans/{loanId}/payments")
    public ResponseEntity<LoanPaymentResponseDTO> registerLoanPayment(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId,
            @Valid @RequestBody LoanPaymentRequestDTO requestDTO) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        Money amount = Money.of(requestDTO.getAmount(), application.domain.valueobjects.Currency.COP);
        Loan updated = naturalCustomerPort.registerLoanPayment(authenticatedUser, loan, amount);
        
        // For now return a simple response
        LoanPaymentResponseDTO response = new LoanPaymentResponseDTO();
        response.setLoanId(loanId);
        response.setAmountPaid(requestDTO.getAmount());
        response.setPaymentDate(java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDTO> createTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody CreateTransferRequestDTO requestDTO) {
        
        Transfer transfer = TransferRestMapper.toDomain(requestDTO);
        Transfer created = naturalCustomerPort.createTransfer(authenticatedUser, transfer);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransferRestMapper.toResponseDTO(created));
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponseDTO>> consultMyOperations(@AuthenticationPrincipal User authenticatedUser) {
        List<Operation> operations = naturalCustomerPort.consultMyOperations(authenticatedUser);
        return ResponseEntity.ok(operations.stream()
                .map(OperationRestMapper::toResponseDTO)
                .toList());
    }
}