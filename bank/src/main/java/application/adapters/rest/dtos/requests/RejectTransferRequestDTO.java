package application.adapters.rest.dtos.requests;

import lombok.Data;

@Data
public class RejectTransferRequestDTO {

    private String rejectionReason;
}