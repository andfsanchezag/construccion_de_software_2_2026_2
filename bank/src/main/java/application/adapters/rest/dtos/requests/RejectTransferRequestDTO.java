package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectTransferRequestDTO {

    @Size(max = 500)
    private String rejectionReason;
}