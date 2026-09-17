package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.CreateTransferRequestDTO;
import application.adapters.rest.dtos.responses.BankAccountResponseDTO;
import application.adapters.rest.dtos.responses.TransferResponseDTO;
import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.adapters.rest.mappers.BankAccountRestMapper;
import application.adapters.rest.mappers.TransferRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.domain.models.Transfer;
import application.domain.models.BankAccount;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.BusinessOperatorPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business-operator")
public class BusinessOperatorRestController {

    private final BusinessOperatorPort businessOperatorPort;

    public BusinessOperatorRestController(BusinessOperatorPort businessOperatorPort) {
        this.businessOperatorPort = businessOperatorPort;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccountResponseDTO>> consultCompanyAccounts(@AuthenticationPrincipal User authenticatedUser) {
        List<BankAccount> accounts = businessOperatorPort.consultCompanyAccounts(authenticatedUser);
        return ResponseEntity.ok(accounts.stream()
                .map(BankAccountRestMapper::toResponseDTO)
                .toList());
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponseDTO> createCompanyTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody CreateTransferRequestDTO requestDTO) {
        
        Transfer transfer = TransferRestMapper.toDomain(requestDTO);
        Transfer created = businessOperatorPort.createCompanyTransfer(authenticatedUser, transfer);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(TransferRestMapper.toResponseDTO(created));
    }

    @PostMapping("/transfers/{transferId}/submit")
    public ResponseEntity<TransferResponseDTO> submitTransferForApproval(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String transferId) {
        
        Transfer transfer = new Transfer();
        transfer.setIdentifier(transferId);
        Transfer submitted = businessOperatorPort.submitTransferForApproval(authenticatedUser, transfer);
        return ResponseEntity.ok(TransferRestMapper.toResponseDTO(submitted));
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponseDTO>> consultCompanyOperations(@AuthenticationPrincipal User authenticatedUser) {
        List<Operation> operations = businessOperatorPort.consultCompanyOperations(authenticatedUser);
        return ResponseEntity.ok(operations.stream()
                .map(OperationRestMapper::toResponseDTO)
                .toList());
    }
}