package application.adapters.useCases;

import application.domain.models.BankAccount;
import application.domain.models.CustomerProducts;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.BusinessOperatorPort;
import application.domain.services.customer.ConsultCustomerProductsService;
import application.domain.services.transfer.CreateTransferService;
import application.domain.services.operation.ConsultOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessOperatorUseCaseImpl implements BusinessOperatorPort {

    private final ConsultCustomerProductsService consultCustomerProductsService;
    private final CreateTransferService createTransferService;
    private final ConsultOperationsService consultOperationsService;

    @Override
    public List<BankAccount> consultCompanyAccounts(User user) {
        CustomerProducts products = consultCustomerProductsService.consultCustomerProducts(user, user.getCustomer());
        return products.getAccounts();
    }

    @Override
    public Transfer createCompanyTransfer(User user, Transfer transfer) {
        return createTransferService.execute(transfer, user, user.getCustomer());
    }

    @Override
    public Transfer submitTransferForApproval(User user, Transfer transfer) {
        // Creating a transfer with high amount automatically requires approval
        return createTransferService.execute(transfer, user, user.getCustomer());
    }

    @Override
    public List<Operation> consultCompanyOperations(User user) {
        return consultOperationsService.executeByUser(user, user);
    }
}