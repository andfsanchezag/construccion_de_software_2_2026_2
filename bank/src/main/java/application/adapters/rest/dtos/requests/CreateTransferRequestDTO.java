package application.adapters.rest.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransferRequestDTO {

    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String description;
}