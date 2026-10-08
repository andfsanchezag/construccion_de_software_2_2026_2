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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/natural-customer")
public class NaturalCustomerRestController {

    private static final Logger log = LoggerFactory.getLogger(NaturalCustomerRestController.class);

    private final NaturalCustomerPort naturalCustomerPort;

    public NaturalCustomerRestController(NaturalCustomerPort naturalCustomerPort) {
        this.naturalCustomerPort = naturalCustomerPort;
    }

    @GetMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> consultMyProfile(@AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        Customer customer = naturalCustomerPort.consultMyProfile(authenticatedUser);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(customer));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerResponseDTO> updateMyProfile(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @Valid @RequestBody UpdateCustomerProfileRequestDTO requestDTO) {
        
        if (authenticatedUser == null || authenticatedUser.getCustomer() == null
                || authenticatedUser.getCustomer().getIdentification() == null) {
            log.warn("Actualización de perfil rechazada: token sin customer asociado. Usuario='{}'. "
                    + "Haga login de nuevo con un usuario NATURAL_CUSTOMER.",
                    authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN");
            throw new application.domain.exceptions.UnauthorizedOperationException(
                    "Authenticated natural customer with associated customer is required.");
        }
        Customer customer = authenticatedUser.getCustomer();
        CustomerRestMapper.updateDomainFromDTO(requestDTO, customer);
        Customer updated = naturalCustomerPort.updateMyProfile(authenticatedUser, customer);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(updated));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponseDTO>> consultMyAccounts(@AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        List<BankAccount> accounts = naturalCustomerPort.consultMyAccounts(authenticatedUser);
        return ResponseEntity.ok(accounts.stream()
                .map(BankAccountRestMapper::toResponseDTO)
                .toList());
    }

    @GetMapping("/products")
    public ResponseEntity<CustomerProductsResponseDTO> consultMyProducts(@AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        application.domain.models.CustomerProducts products = naturalCustomerPort.consultMyProducts(authenticatedUser);
        return ResponseEntity.ok(CustomerProductsRestMapper.toResponseDTO(products));
    }

    @GetMapping("/accounts/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponseDTO> consultAccountBalance(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
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

    @PostMapping({"/loans", "/loan"})
    public ResponseEntity<LoanResponseDTO> requestLoan(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @Valid @RequestBody RequestLoanRequestDTO requestDTO,
            jakarta.servlet.http.HttpServletRequest httpRequest) {

        if (httpRequest != null && httpRequest.getRequestURI() != null
                && httpRequest.getRequestURI().endsWith("/loan")) {
            log.warn("Se usó el alias singular POST /api/v1/natural-customer/loan; la ruta canónica es "
                    + "POST /api/v1/natural-customer/loans. Usuario del token: identification='{}' username='{}'.",
                    authenticatedUser != null && authenticatedUser.getCustomer() != null
                            ? authenticatedUser.getCustomer().getIdentification() : "UNKNOWN",
                    authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN");
        }
        log.info("Solicitud de préstamo: usuario='{}' customerIdentification='{}' loanType='{}' amount='{}' term='{}' destAccount='{}' currency='{}'",
                authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN",
                authenticatedUser != null && authenticatedUser.getCustomer() != null
                        ? authenticatedUser.getCustomer().getIdentification() : "UNKNOWN",
                requestDTO.getLoanType(), requestDTO.getRequestedAmount(),
                requestDTO.getTermInMonths(), requestDTO.getDestinationAccountNumber(),
                requestDTO.getCurrency());
        Loan loan = LoanRestMapper.toDomain(requestDTO);
        Loan requested = naturalCustomerPort.requestLoan(authenticatedUser, loan);
        log.info("Préstamo creado: loanId='{}' applicant='{}' status='{}'",
                requested.getIdentifier(),
                requested.getApplicant() != null ? requested.getApplicant().getIdentification() : "UNKNOWN",
                requested.getLoanStatus() != null ? requested.getLoanStatus().getCode() : "UNKNOWN");
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanRestMapper.toResponseDTO(requested));
    }

    @GetMapping({"/loans/{loanId}", "/loan/{loanId}"})
    public ResponseEntity<LoanResponseDTO> consultLoan(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String loanId) {

        log.info("Consulta de préstamo: loanId='{}' por usuario='{}'", loanId,
                authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN");
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        Loan found = naturalCustomerPort.consultLoan(authenticatedUser, loan);
        return ResponseEntity.ok(LoanRestMapper.toResponseDTO(found));
    }

    @PostMapping({"/loans/{loanId}/payments", "/loan/{loanId}/payments"})
    public ResponseEntity<LoanPaymentResponseDTO> registerLoanPayment(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @PathVariable String loanId,
            @Valid @RequestBody LoanPaymentRequestDTO requestDTO) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        BankAccount sourceAccount = null;
        if (requestDTO.getSourceAccountNumber() != null && !requestDTO.getSourceAccountNumber().isBlank()) {
            sourceAccount = new BankAccount();
            sourceAccount.setIdentifier(requestDTO.getSourceAccountNumber());
        }
        // La moneda se ajusta a la cuenta origen en el servicio si difiere; por defecto COP.
        Money amount = Money.of(requestDTO.getAmount(), application.domain.valueobjects.Currency.COP);
        Loan updated = naturalCustomerPort.registerLoanPayment(authenticatedUser, loan, sourceAccount, amount);
        
        LoanPaymentResponseDTO response = new LoanPaymentResponseDTO();
        response.setLoanId(updated.getIdentifier() != null ? updated.getIdentifier() : loanId);
        response.setAmountPaid(requestDTO.getAmount());
        response.setPaymentDate(java.time.LocalDateTime.now());
        log.info("Pago de préstamo OK: loanId='{}' amount='{}' por='{}'", response.getLoanId(),
                requestDTO.getAmount(),
                authenticatedUser != null ? authenticatedUser.getUsername() : "UNKNOWN");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDTO> createTransfer(
            @AuthenticationPrincipal(expression = "user") User authenticatedUser,
            @Valid @RequestBody CreateTransferRequestDTO requestDTO) {
        
        Transfer transfer = TransferRestMapper.toDomain(requestDTO);
        Transfer created = naturalCustomerPort.createTransfer(authenticatedUser, transfer);
        return ResponseEntity.status(HttpStatus.CREATED).body(TransferRestMapper.toResponseDTO(created));
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponseDTO>> consultMyOperations(@AuthenticationPrincipal(expression = "user") User authenticatedUser) {
        List<Operation> operations = naturalCustomerPort.consultMyOperations(authenticatedUser);
        return ResponseEntity.ok(operations.stream()
                .map(OperationRestMapper::toResponseDTO)
                .toList());
    }
}