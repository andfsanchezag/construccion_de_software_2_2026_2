package application.adapters.rest.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangeCustomerStatusRequestDTO {

    @Size(max = 40)
    @NotBlank
    @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED")
    private String status;
    private String reason;
}