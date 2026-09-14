package application.adapters.rest.dtos.responses;

import lombok.Data;

@Data
public class BankAccountResponseDTO {

    private String accountNumber;
    private String accountType;
    private String currency;
    private String status;
    private Double currentBalance;
    private String ownerIdentification;
}