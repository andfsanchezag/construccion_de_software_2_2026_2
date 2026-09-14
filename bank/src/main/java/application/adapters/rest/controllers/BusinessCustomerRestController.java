package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.RegisterCompanyUserRequestDTO;
import application.adapters.rest.dtos.requests.RequestLoanRequestDTO;
import application.adapters.rest.dtos.requests.RejectTransferRequestDTO;
import application.adapters.rest.dtos.responses.BusinessCustomerResponseDTO;
import application.adapters.rest.dtos.responses.CustomerProductsResponseDTO;
import application.adapters.rest.dtos.responses.BankAccountResponseDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.adapters.rest.dtos.responses.TransferResponseDTO;
import application.adapters.rest.mappers.CustomerRestMapper;
import application.adapters.rest.mappers.CustomerProductsRestMapper;
import application.adapters.rest.mappers.BankAccountRestMapper;
import application.adapters.rest.mappers.LoanRestMapper;
import application.adapters.rest.mappers.TransferRestMapper;
import application.domain.models.Loan;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.BusinessCustomerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business-customer")
public class BusinessCustomerRestController {

    private final BusinessCustomerPort businessCustomerPort;

    public BusinessCustomerRestController(BusinessCustomerPort businessCustomerPort) {
        this.businessCustomerPort = businessCustomerPort;
    }

    @GetMapping("/profile")
    public ResponseEntity<BusinessCustomerResponseDTO> consultCompanyProfile(@AuthenticationPrincipal User authenticatedUser) {
        application.domain.models.BusinessCustomer profile = businessCustomerPort.consultCompanyProfile(authenticatedUser);
        return ResponseEntity.ok(CustomerRestMapper.toBusinessResponseDTO(profile));
    }

    @GetMapping("/products")
    public ResponseEntity<CustomerProductsResponseDTO> consultCompanyProducts(@AuthenticationPrincipal User authenticatedUser) {
        application.domain.models.CustomerProducts products = businessCustomerPort.consultCompanyProducts(authenticatedUser);
        return ResponseEntity.ok(CustomerProductsRestMapper.toResponseDTO(products));
    }

    @PostMapping("/users")
    public ResponseEntity<application.adapters.rest.dtos.responses.UserResponseDTO> registerCompanyUser(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody RegisterCompanyUserRequestDTO requestDTO) {
        
        User newUser = new User();
        newUser.setUsername(requestDTO.getUsername());
        newUser.setPassword(requestDTO.getPassword());
        newUser.setEmail(requestDTO.getEmail());
        
        if (requestDTO.getRole() != null) {
            newUser.setRole(application.domain.valueobjects.SystemRole.fromCode(requestDTO.getRole()));
        }
        
        application.domain.models.Person person = new application.domain.models.Person();
        person.setIdentification(requestDTO.getIdentification());
        person.setName(requestDTO.getName());
        newUser.setPerson(person);
        
        User created = businessCustomerPort.registerCompanyUser(authenticatedUser, newUser);
        
        application.adapters.rest.dtos.responses.UserResponseDTO response = new application.adapters.rest.dtos.responses.UserResponseDTO();
        response.setUserId(created.getUserId());
        response.setUsername(created.getUsername());
        response.setRole(created.getRole() != null ? created.getRole().getCode() : null);
        response.setStatus(created.getStatus() != null ? created.getStatus().getCode() : null);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponseDTO>> consultCompanyAccounts(@AuthenticationPrincipal User authenticatedUser) {
        List<application.domain.models.BankAccount> accounts = businessCustomerPort.consultCompanyAccounts(authenticatedUser);
        return ResponseEntity.ok(accounts.stream()
                .map(BankAccountRestMapper::toResponseDTO)
                .toList());
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponseDTO> requestCompanyLoan(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody RequestLoanRequestDTO requestDTO) {
        
        Loan loan = LoanRestMapper.toDomain(requestDTO);
        Loan requested = businessCustomerPort.requestCompanyLoan(authenticatedUser, loan);
        return ResponseEntity.status(HttpStatus.CREATED).body(LoanRestMapper.toResponseDTO(requested));
    }

    @PatchMapping("/transfers/{transferId}/approve")
    public ResponseEntity<TransferResponseDTO> approveCompanyTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String transferId) {
        
        Transfer transfer = new Transfer();
        transfer.setIdentifier(transferId);
        Transfer approved = businessCustomerPort.approveCompanyTransfer(authenticatedUser, transfer);
        return ResponseEntity.ok(TransferRestMapper.toResponseDTO(approved));
    }

    @PatchMapping("/transfers/{transferId}/reject")
    public ResponseEntity<TransferResponseDTO> rejectCompanyTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String transferId,
            @Valid @RequestBody RejectTransferRequestDTO requestDTO) {
        
        Transfer transfer = new Transfer();
        transfer.setIdentifier(transferId);
        Transfer rejected = businessCustomerPort.rejectCompanyTransfer(authenticatedUser, transfer);
        return ResponseEntity.ok(TransferRestMapper.toResponseDTO(rejected));
    }
}