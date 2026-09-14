package application.adapters.rest.mappers;

import application.adapters.rest.dtos.responses.CustomerProductsResponseDTO;
import application.domain.models.CustomerProducts;
import lombok.experimental.UtilityClass;

import java.util.stream.Collectors;

@UtilityClass
public class CustomerProductsRestMapper {

    public CustomerProductsResponseDTO toResponseDTO(CustomerProducts products) {
        if (products == null) {
            return null;
        }
        
        CustomerProductsResponseDTO dto = new CustomerProductsResponseDTO();
        dto.setAccounts(products.getAccounts().stream()
                .map(BankAccountRestMapper::toResponseDTO)
                .collect(Collectors.toList()));
        dto.setLoans(products.getLoans().stream()
                .map(LoanRestMapper::toResponseDTO)
                .collect(Collectors.toList()));
        dto.setTransfers(products.getTransfers().stream()
                .map(TransferRestMapper::toResponseDTO)
                .collect(Collectors.toList()));
        return dto;
    }
}