package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanPaymentResponseDTO {

    private String loanId;
    private BigDecimal amountPaid;
    private BigDecimal remainingBalance;
    private LocalDateTime paymentDate;
}