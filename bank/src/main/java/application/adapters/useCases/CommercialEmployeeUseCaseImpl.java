package application.adapters.useCases;

import application.domain.models.BankAccount;
import application.domain.models.Customer;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.in.CommercialEmployeePort;
import application.domain.services.customer.ConsultCustomerProductsService;
import application.domain.services.customer.ConsultCustomerService;
import application.domain.services.customer.UpdateCustomerService;
import application.domain.services.loan.ConsultLoanService;
import application.domain.services.loan.RequestLoanService;
import application.domain.services.account.OpenBankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommercialEmployeeUseCaseImpl implements CommercialEmployeePort {

    private final ConsultCustomerService consultCustomerService;
    private final UpdateCustomerService updateCustomerService;
    private final ConsultCustomerProductsService consultCustomerProductsService;
    private final RequestLoanService requestLoanService;
    private final ConsultLoanService consultLoanService;
    private final OpenBankAccountService openBankAccountService;

    @Override
    public Customer consultCustomer(User user, Customer customer) {
        return consultCustomerService.consultCustomer(user, customer);
    }

    @Override
    public Customer updateCustomer(User user, Customer customer) {
        return updateCustomerService.updateCustomer(user, customer);
    }

    @Override
    public CustomerProducts consultCustomerProducts(User user, Customer customer) {
        return consultCustomerProductsService.consultCustomerProducts(user, customer);
    }

    @Override
    public Loan requestLoanOnBehalfOfCustomer(User user, Customer customer, Loan loan) {
        loan.setApplicant(customer);
        return requestLoanService.request(user, loan);
    }

    @Override
    public Loan consultLoanStatus(User user, Loan loan) {
        return consultLoanService.consult(user, loan);
    }

    @Override
    public BankAccount openBankAccount(User user, BankAccount account) {
        return openBankAccountService.open(user, account);
    }
}