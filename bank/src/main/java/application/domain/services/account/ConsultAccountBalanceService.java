package application.domain.services.account;

import application.domain.exceptions.EntityNotFoundException;
import application.domain.models.BankAccount;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConsultAccountBalanceService {

    private final BankAccountRepositoryPort bankAccountRepositoryPort;

    public BigDecimal execute(User requestingUser, BankAccount account) {
        Optional<BankAccount> found = bankAccountRepositoryPort.findByIdentifier(account);
        if (found.isEmpty()) {
            throw new EntityNotFoundException("BankAccount");
        }
        return found.get().getCurrentBalance();
    }
}
