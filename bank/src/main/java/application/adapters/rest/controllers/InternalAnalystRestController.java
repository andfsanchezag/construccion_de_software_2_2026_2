package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.RegisterEmployeeUserRequestDTO;
import application.adapters.rest.dtos.requests.ChangeCustomerStatusRequestDTO;
import application.adapters.rest.dtos.requests.ApproveLoanRequestDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.adapters.rest.dtos.responses.CustomerResponseDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.adapters.rest.dtos.responses.AuditLogPageResponseDTO;
import application.adapters.rest.mappers.CustomerRestMapper;
import application.adapters.rest.mappers.LoanRestMapper;
import application.adapters.rest.mappers.AuditLogRestMapper;
import application.domain.models.AuditLog;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.InternalAnalystPort;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/internal-analyst")
public class InternalAnalystRestController {

    private final InternalAnalystPort internalAnalystPort;

    public InternalAnalystRestController(InternalAnalystPort internalAnalystPort) {
        this.internalAnalystPort = internalAnalystPort;
    }

    @PostMapping("/users/employee")
    public ResponseEntity<UserResponseDTO> registerEmployeeUser(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody RegisterEmployeeUserRequestDTO requestDTO) {
        
        User newEmployee = new User();
        newEmployee.setUsername(requestDTO.getUsername());
        newEmployee.setPassword(requestDTO.getPassword());
        newEmployee.setEmail(requestDTO.getEmail());
        
        if (requestDTO.getRole() != null) {
            newEmployee.setRole(application.domain.valueobjects.SystemRole.fromCode(requestDTO.getRole()));
        }
        
        application.domain.models.Person person = new application.domain.models.Person();
        person.setIdentification(requestDTO.getIdentification());
        person.setName(requestDTO.getName());
        newEmployee.setPerson(person);
        
        User created = internalAnalystPort.registerEmployeeUser(authenticatedUser, newEmployee);
        
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(created.getUserId());
        response.setUsername(created.getUsername());
        response.setRole(created.getRole() != null ? created.getRole().getCode() : null);
        response.setStatus(created.getStatus() != null ? created.getStatus().getCode() : null);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/customers/{identification}/status")
    public ResponseEntity<CustomerResponseDTO> changeCustomerStatus(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String identification,
            @Valid @RequestBody ChangeCustomerStatusRequestDTO requestDTO) {
        
        Customer customer = new Customer();
        customer.setIdentification(identification);
        
        CustomerStatus newStatus = CustomerStatus.fromCode(requestDTO.getStatus());
        Customer updated = internalAnalystPort.changeCustomerStatus(authenticatedUser, customer, newStatus);
        return ResponseEntity.ok(CustomerRestMapper.toResponseDTO(updated));
    }

    @PatchMapping("/loans/{loanId}/approve")
    public ResponseEntity<LoanResponseDTO> approveLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId,
            @Valid @RequestBody ApproveLoanRequestDTO requestDTO) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        LoanRestMapper.applyApproval(loan, requestDTO);
        
        Loan approved = internalAnalystPort.approveLoan(authenticatedUser, loan);
        return ResponseEntity.ok(LoanRestMapper.toResponseDTO(approved));
    }

    @PatchMapping("/loans/{loanId}/reject")
    public ResponseEntity<LoanResponseDTO> rejectLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        Loan rejected = internalAnalystPort.rejectLoan(authenticatedUser, loan);
        return ResponseEntity.ok(LoanRestMapper.toResponseDTO(rejected));
    }

    @PostMapping("/loans/{loanId}/disburse")
    public ResponseEntity<LoanResponseDTO> disburseLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        Loan disbursed = internalAnalystPort.disburseLoan(authenticatedUser, loan, null);
        return ResponseEntity.ok(LoanRestMapper.toResponseDTO(disbursed));
    }

    @DeleteMapping("/loans/{loanId}")
    public ResponseEntity<Void> deleteLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String loanId) {
        
        Loan loan = new Loan();
        loan.setIdentifier(loanId);
        internalAnalystPort.closeLoan(authenticatedUser, loan);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<AuditLogPageResponseDTO> consultAuditLog(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String operationType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<AuditLog> auditLogs = internalAnalystPort.consultAuditLog(authenticatedUser);
        
        // Apply filters if provided
        if (userId != null) {
            auditLogs = auditLogs.stream()
                    .filter(log -> userId.equals(log.getPerformedBy()))
                    .collect(Collectors.toList());
        }
        if (operationType != null) {
            auditLogs = auditLogs.stream()
                    .filter(log -> operationType.equals(log.getOperationType() != null ? log.getOperationType().getCode() : null))
                    .collect(Collectors.toList());
        }
        
        // Pagination
        int start = page * size;
        int end = Math.min(start + size, auditLogs.size());
        List<AuditLog> pageContent = start < auditLogs.size() ? auditLogs.subList(start, end) : List.of();
        
        AuditLogPageResponseDTO response = new AuditLogPageResponseDTO();
        response.setContent(pageContent.stream()
                .map(AuditLogRestMapper::toResponseDTO)
                .collect(Collectors.toList()));
        response.setTotalElements(auditLogs.size());
        response.setTotalPages((int) Math.ceil((double) auditLogs.size() / size));
        response.setPageNumber(page);
        response.setPageSize(size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/operations")
    public ResponseEntity<List<application.adapters.rest.dtos.responses.OperationResponseDTO>> consultAllOperations(
            @AuthenticationPrincipal User authenticatedUser) {
        
        List<Operation> operations = internalAnalystPort.consultAllOperations(authenticatedUser);
        return ResponseEntity.ok(operations.stream()
                .map(application.adapters.rest.mappers.OperationRestMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }
}