package application.domain.services.authorization;

import application.domain.models.BusinessCustomer;
import application.domain.models.BankingProduct;
import application.domain.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizeBusinessCustomerOperationService {

    private final ValidateBusinessOperatorAuthorizationService validateBusinessOperatorAuthorizationService;

    public void execute(User user, BusinessCustomer customer, BankingProduct product) {
        validateBusinessOperatorAuthorizationService.execute(user, customer, product);
    }
}
