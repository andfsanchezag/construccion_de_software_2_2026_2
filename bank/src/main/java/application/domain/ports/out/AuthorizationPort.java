package application.domain.ports.out;

import application.domain.models.BankingProduct;
import application.domain.models.Customer;
import application.domain.models.User;

public interface AuthorizationPort {

    boolean isAuthorized(User user, Customer customer);

    boolean canOperateOn(User user, BankingProduct product);

    boolean canApprove(User user, BankingProduct product);
}
