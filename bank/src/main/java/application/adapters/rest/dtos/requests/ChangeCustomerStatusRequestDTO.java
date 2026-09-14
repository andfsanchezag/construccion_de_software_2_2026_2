package application.adapters.rest.dtos.requests;

import lombok.Data;

@Data
public class ChangeCustomerStatusRequestDTO {

    private String status;
    private String reason;
}