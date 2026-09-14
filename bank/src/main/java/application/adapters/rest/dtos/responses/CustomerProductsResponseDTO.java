package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.util.List;

@Data
public class CustomerProductsResponseDTO {

    private List<BankAccountResponseDTO> accounts;
    private List<LoanResponseDTO> loans;
    private List<TransferResponseDTO> transfers;
}