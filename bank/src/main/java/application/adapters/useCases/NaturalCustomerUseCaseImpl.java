package application.adapters.useCases;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.NaturalCustomerPort;
import application.domain.services.account.ConsultAccountBalanceService;
import application.domain.services.account.ConsultBankAccountService;
import application.domain.services.customer.ConsultCustomerProductsService;
import application.domain.services.customer.ConsultCustomerService;
import application.domain.services.customer.UpdateCustomerService;
import application.domain.services.loan.ConsultLoanService;
import application.domain.services.loan.RequestLoanService;
import application.domain.services.operation.ConsultOperationsService;
import application.domain.services.transfer.CreateTransferService;
import application.domain.services.transfer.ExecuteTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NaturalCustomerUseCaseImpl implements NaturalCustomerPort {

    private final ConsultCustomerService consultCustomerService;
    private final UpdateCustomerService updateCustomerService;
    private final ConsultCustomerProductsService consultCustomerProductsService;
    private final ConsultBankAccountService consultBankAccountService;
    private final ConsultAccountBalanceService consultAccountBalanceService;
    private final RequestLoanService requestLoanService;
    private final ConsultLoanService consultLoanService;
    private final CreateTransferService createTransferService;
    private final ExecuteTransferService executeTransferService;
    private final ConsultOperationsService consultOperationsService;

    @Override
    public Customer consultMyProfile(User user) {
        Customer customer = user.getCustomer();
        return consultCustomerService.consultCustomer(user, customer);
    }

    @Override
    public Customer updateMyProfile(User user, Customer customer) {
        customer.setIdentification(user.getCustomer().getIdentification());
        return updateCustomerService.updateCustomer(user, customer);
    }

    @Override
    public CustomerProducts consultMyProducts(User user) {
        Customer customer = user.getCustomer();
        return consultCustomerProductsService.consultCustomerProducts(user, customer);
    }

    @Override
    public List<BankAccount> consultMyAccounts(User user) {
        Customer customer = user.getCustomer();
        CustomerProducts products = consultCustomerProductsService.consultCustomerProducts(user, customer);
        return products.getAccounts();
    }

    @Override
    public application.domain.valueobjects.Money consultAccountBalance(User user, BankAccount account) {
        return consultAccountBalanceService.consultBalance(user, account);
    }

    @Override
    public Loan requestLoan(User user, Loan loan) {
        loan.setApplicant(user.getCustomer());
        return requestLoanService.request(user, loan);
    }

    @Override
    public Loan consultLoan(User user, Loan loan) {
        return consultLoanService.consult(user, loan);
    }

    @Override
    public Loan registerLoanPayment(User user, Loan loan, application.domain.valueobjects.Money amount) {
        // This would require a RegisterLoanPaymentService which doesn't exist yet
        // For now, delegate to the existing service structure
        throw new UnsupportedOperationException("Loan payment registration not yet implemented");
    }

    @Override
    public Transfer createTransfer(User user, Transfer transfer) {
        Customer customer = user.getCustomer();
        return createTransferService.execute(transfer, user, customer);
    }

    @Override
    public Transfer executeTransfer(User user, Transfer transfer) {
        return executeTransferService.execute(user, transfer);
    }

    @Override
    public List<Operation> consultMyOperations(User user) {
        return consultOperationsService.executeByUser(user, user);
    }
}