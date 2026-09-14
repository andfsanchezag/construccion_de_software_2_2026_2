package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.RequestLoanRequestDTO;
import application.adapters.rest.dtos.requests.CommercialRequestLoanRequestDTO;
import application.adapters.rest.dtos.requests.ApproveLoanRequestDTO;
import application.adapters.rest.dtos.responses.LoanResponseDTO;
import application.domain.models.Loan;
import application.domain.models.Customer;
import application.domain.models.BankAccount;
import application.domain.valueobjects.LoanType;
import application.domain.valueobjects.LoanStatus;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@UtilityClass
public class LoanRestMapper {

    public Loan toDomain(RequestLoanRequestDTO dto) {
        Loan loan = new Loan();
        if (dto.getLoanType() != null) {
            loan.setLoanType(LoanType.fromCode(dto.getLoanType()));
        }
        loan.setRequestedAmount(dto.getRequestedAmount());
        loan.setTermInMonths(dto.getTermInMonths());
        
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
        dto.setApplicationDate(loan.getApplicationDate());
        dto.setApprovalDate(loan.getApprovalDate());
        return dto;
    }
}