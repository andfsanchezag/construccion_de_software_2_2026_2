package application.adapters.rest.mappers;

import application.adapters.rest.controllers.TellerEmployeeRestController;
import application.adapters.rest.dtos.responses.BankAccountResponseDTO;
import application.adapters.rest.dtos.responses.AccountBalanceResponseDTO;
import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.valueobjects.AccountStatus;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.Money;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class BankAccountRestMapper {

    public BankAccountResponseDTO toResponseDTO(BankAccount account) {
        if (account == null) {
            return null;
        }
        
        BankAccountResponseDTO dto = new BankAccountResponseDTO();
        dto.setAccountNumber(account.getIdentifier());
        dto.setAccountType(account.getAccountType() != null ? account.getAccountType().getCode() : null);
        dto.setCurrency(account.getCurrency() != null ? account.getCurrency().getCode() : null);
        dto.setStatus(account.getAccountStatus() != null ? account.getAccountStatus().getCode() : null);
        dto.setCurrentBalance(account.getCurrentBalance() != null ? account.getCurrentBalance().doubleValue() : null);
        dto.setOwnerIdentification(account.getOwner() != null ? account.getOwner().getIdentification() : null);
        return dto;
    }

    public AccountBalanceResponseDTO toBalanceResponseDTO(BankAccount account) {
        if (account == null) {
            return null;
        }
        
        AccountBalanceResponseDTO dto = new AccountBalanceResponseDTO();
        dto.setAccountNumber(account.getIdentifier());
        dto.setAvailableBalance(account.getCurrentBalance() != null ? account.getCurrentBalance().doubleValue() : null);
        dto.setCurrency(account.getCurrency() != null ? account.getCurrency().getCode() : null);
        return dto;
    }

    public BankAccount toDomain(TellerEmployeeRestController.BankAccountRequestDTO dto) {
        BankAccount account = new BankAccount();
        if (dto.getAccountType() != null) {
            account.setAccountType(AccountType.fromCode(dto.getAccountType()));
        }
        if (dto.getCurrency() != null) {
            account.setCurrency(Currency.fromCode(dto.getCurrency()));
        }
        if (dto.getInitialBalance() != null) {
            account.setCurrentBalance(dto.getInitialBalance());
        }
        
        if (dto.getOwnerIdentification() != null) {
            Customer owner = new Customer();
            owner.setIdentification(dto.getOwnerIdentification());
            account.setOwner(owner);
        }
        return account;
    }
}