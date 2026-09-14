package application.adapters.rest.dtos.requests;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequestDTO {

    private BigDecimal amount;
    private String reference;
}