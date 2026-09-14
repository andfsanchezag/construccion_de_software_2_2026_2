package application.adapters.useCases;

import application.domain.models.BankAccount;
import application.domain.models.CustomerProducts;
import application.domain.models.Loan;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.models.BusinessCustomer;
import application.domain.ports.in.BusinessCustomerPort;
import application.domain.services.customer.ConsultCustomerProductsService;
import application.domain.services.customer.ConsultCustomerService;
import application.domain.services.loan.RequestLoanService;
import application.domain.services.transfer.ApproveTransferService;
import application.domain.services.transfer.RejectTransferService;
import application.domain.services.user.RegisterCustomerUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessCustomerUseCaseImpl implements BusinessCustomerPort {

    private final ConsultCustomerService consultCustomerService;
    private final ConsultCustomerProductsService consultCustomerProductsService;
    private final RegisterCustomerUserService registerCustomerUserService;
    private final RequestLoanService requestLoanService;
    private final ApproveTransferService approveTransferService;
    private final RejectTransferService rejectTransferService;

    @Override
    public BusinessCustomer consultCompanyProfile(User user) {
        return (BusinessCustomer) consultCustomerService.consultCustomer(user, user.getCustomer());
    }

    @Override
    public CustomerProducts consultCompanyProducts(User user) {
        return consultCustomerProductsService.consultCustomerProducts(user, user.getCustomer());
    }

    @Override
    public User registerCompanyUser(User user, User newCompanyUser) {
        newCompanyUser.setCustomer(user.getCustomer());
        return registerCustomerUserService.registerCustomerUser(user, newCompanyUser);
    }

    @Override
    public List<BankAccount> consultCompanyAccounts(User user) {
        CustomerProducts products = consultCustomerProductsService.consultCustomerProducts(user, user.getCustomer());
        return products.getAccounts();
    }

    @Override
    public Loan requestCompanyLoan(User user, Loan loan) {
        loan.setApplicant(user.getCustomer());
        return requestLoanService.request(user, loan);
    }

    @Override
    public Transfer approveCompanyTransfer(User user, Transfer transfer) {
        return approveTransferService.execute(user, transfer);
    }

    @Override
    public Transfer rejectCompanyTransfer(User user, Transfer transfer) {
        return rejectTransferService.execute(user, transfer);
    }
}