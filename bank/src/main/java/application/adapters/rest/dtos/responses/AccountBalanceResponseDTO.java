package application.adapters.rest.dtos.responses;

import lombok.Data;

@Data
public class AccountBalanceResponseDTO {

    private String accountNumber;
    private Double availableBalance;
    private String currency;
}