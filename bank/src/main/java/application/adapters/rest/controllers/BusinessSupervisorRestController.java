package application.adapters.rest.controllers;

import application.adapters.rest.dtos.responses.TransferResponseDTO;
import application.adapters.rest.dtos.responses.OperationResponseDTO;
import application.adapters.rest.mappers.TransferRestMapper;
import application.adapters.rest.mappers.OperationRestMapper;
import application.domain.models.Transfer;
import application.domain.models.Operation;
import application.domain.models.User;
import application.domain.ports.in.BusinessSupervisorPort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business-supervisor")
public class BusinessSupervisorRestController {

    private final BusinessSupervisorPort businessSupervisorPort;

    public BusinessSupervisorRestController(BusinessSupervisorPort businessSupervisorPort) {
        this.businessSupervisorPort = businessSupervisorPort;
    }

    @GetMapping("/transfers/pending")
    public ResponseEntity<List<TransferResponseDTO>> consultPendingTransfers(@AuthenticationPrincipal User authenticatedUser) {
        List<Transfer> transfers = businessSupervisorPort.consultPendingTransfers(authenticatedUser);
        return ResponseEntity.ok(transfers.stream()
                .map(TransferRestMapper::toResponseDTO)
                .toList());
    }

    @PatchMapping("/transfers/{transferId}/approve")
    public ResponseEntity<TransferResponseDTO> approveTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String transferId) {
        
        Transfer transfer = new Transfer();
        transfer.setIdentifier(transferId);
        Transfer approved = businessSupervisorPort.approveTransfer(authenticatedUser, transfer);
        return ResponseEntity.ok(TransferRestMapper.toResponseDTO(approved));
    }

    @PatchMapping("/transfers/{transferId}/reject")
    public ResponseEntity<TransferResponseDTO> rejectTransfer(
            @AuthenticationPrincipal User authenticatedUser,
            @PathVariable String transferId) {
        
        Transfer transfer = new Transfer();
        transfer.setIdentifier(transferId);
        Transfer rejected = businessSupervisorPort.rejectTransfer(authenticatedUser, transfer);
        return ResponseEntity.ok(TransferRestMapper.toResponseDTO(rejected));
    }

    @GetMapping("/operations")
    public ResponseEntity<List<OperationResponseDTO>> consultCompanyOperations(@AuthenticationPrincipal User authenticatedUser) {
        List<Operation> operations = businessSupervisorPort.consultCompanyOperations(authenticatedUser);
        return ResponseEntity.ok(operations.stream()
                .map(OperationRestMapper::toResponseDTO)
                .toList());
    }
}