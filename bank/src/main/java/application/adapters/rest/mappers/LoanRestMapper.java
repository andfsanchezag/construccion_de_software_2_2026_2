package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.RequestLoanRequestDTO;
import application.adapters.rest.dtos.requests.CommercialRequestLoanRequestDTO;
import application.adapters.rest.dtos.requests.ApproveLoanRequestDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.domain.models.Loan;
import application.domain.models.Customer;
import application.domain.models.BankAccount;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.LoanType;
import application.domain.valueobjects.LoanStatus;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
public class LoanRestMapper {

    private final Logger log = LoggerFactory.getLogger(LoanRestMapper.class);

    public Loan toDomain(RequestLoanRequestDTO dto) {
        Loan loan = new Loan();
        if (dto.getLoanType() != null) {
            loan.setLoanType(LoanType.fromCode(dto.getLoanType()));
        }
        loan.setRequestedAmount(dto.getRequestedAmount());
        loan.setTermInMonths(dto.getTermInMonths());
        loan.setCurrency(resolveCurrency(dto.getCurrency(), "RequestLoanRequestDTO"));
        
        if (dto.getDestinationAccountNumber() != null) {
            BankAccount destAccount = new BankAccount();
            destAccount.setIdentifier(dto.getDestinationAccountNumber());
            loan.setDestinationAccount(destAccount);
        }
        return loan;
    }

    public Loan toDomain(CommercialRequestLoanRequestDTO dto) {
        Loan loan = new Loan();
        if (dto.getLoanType() != null) {
            loan.setLoanType(LoanType.fromCode(dto.getLoanType()));
        }
        loan.setRequestedAmount(dto.getRequestedAmount());
        loan.setTermInMonths(dto.getTermInMonths());
        loan.setCurrency(resolveCurrency(dto.getCurrency(), "CommercialRequestLoanRequestDTO"));
        
        if (dto.getDestinationAccountNumber() != null) {
            BankAccount destAccount = new BankAccount();
            destAccount.setIdentifier(dto.getDestinationAccountNumber());
            loan.setDestinationAccount(destAccount);
        }
        return loan;
    }

    public void applyApproval(Loan loan, ApproveLoanRequestDTO dto) {
        loan.setApprovedAmount(dto.getApprovedAmount());
        loan.setInterestRate(dto.getInterestRate());
    }

    private Currency resolveCurrency(String code, String source) {
        if (code == null || code.isBlank()) {
            log.info("Mapeo de préstamo desde {} sin 'currency': se asume COP por defecto "
                    + "(evita InvalidLoanException 'Loan currency must be provided').", source);
            return Currency.COP;
        }
        try {
            return Currency.fromCode(code);
        } catch (IllegalArgumentException ex) {
            log.warn("Mapeo de préstamo desde {} con currency desconocida '{}': se asume COP. Detalle: {}",
                    source, code, ex.getMessage());
            return Currency.COP;
        }
    }

    public LoanResponseDTO toResponseDTO(Loan loan) {
        if (loan == null) {
            return null;
        }
        
        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setLoanId(loan.getIdentifier());
        dto.setLoanType(loan.getLoanType() != null ? loan.getLoanType().getCode() : null);
        dto.setRequestedAmount(loan.getRequestedAmount());
        dto.setApprovedAmount(loan.getApprovedAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setStatus(loan.getLoanStatus() != null ? loan.getLoanStatus().getCode() : null);
        dto.setTermInMonths(loan.getTermInMonths());
        dto.setCurrency(loan.getCurrency() != null ? loan.getCurrency().getCode() : null);
        dto.setApplicantIdentification(loan.getApplicant() != null ? loan.getApplicant().getIdentification() : null);
        dto.setDestinationAccountNumber(loan.getDestinationAccount() != null ? loan.getDestinationAccount().getIdentifier() : null);
        dto.setApplicationDate(null);
        dto.setApprovalDate(loan.getApprovalDate() != null ? loan.getApprovalDate().atStartOfDay() : null);
        return dto;
    }
}