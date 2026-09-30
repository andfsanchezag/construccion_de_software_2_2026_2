package application.adapters.rest;

import application.adapters.rest.controllers.CommercialEmployeeRestController;
import application.adapters.rest.controllers.TellerEmployeeRestController;
import application.adapters.rest.dtos.requests.ChangeCustomerStatusRequestDTO;
import application.adapters.rest.dtos.requests.RegisterEmployeeUserRequestDTO;
import application.adapters.rest.dtos.requests.RequestLoanRequestDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestCatalogValidationTest {

    @Test
    void rejectsUnsupportedCatalogCodesAndAcceptsSupportedCodes() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            RequestLoanRequestDTO loanRequest = new RequestLoanRequestDTO();
            loanRequest.setLoanType("personal");
            assertFalse(validator.validate(loanRequest).isEmpty());
            loanRequest.setLoanType("PERSONAL");
            assertTrue(validator.validate(loanRequest).isEmpty());

            RegisterEmployeeUserRequestDTO employeeRequest = new RegisterEmployeeUserRequestDTO();
            employeeRequest.setRole("UNKNOWN_ROLE");
            assertFalse(validator.validate(employeeRequest).isEmpty());
            employeeRequest.setRole("TELLER_EMPLOYEE");
            assertTrue(validator.validate(employeeRequest).isEmpty());

            ChangeCustomerStatusRequestDTO statusRequest = new ChangeCustomerStatusRequestDTO();
            statusRequest.setStatus("SUSPENDED");
            assertFalse(validator.validate(statusRequest).isEmpty());
            statusRequest.setStatus("BLOCKED");
            assertTrue(validator.validate(statusRequest).isEmpty());

            TellerEmployeeRestController.BankAccountRequestDTO tellerAccountRequest =
                    new TellerEmployeeRestController.BankAccountRequestDTO();
            tellerAccountRequest.setAccountType("SAVINGS");
            tellerAccountRequest.setCurrency("COP");
            assertTrue(validator.validate(tellerAccountRequest).isEmpty());
            tellerAccountRequest.setCurrency("BTC");
            assertFalse(validator.validate(tellerAccountRequest).isEmpty());

            CommercialEmployeeRestController.BankAccountRequestDTO commercialAccountRequest =
                    new CommercialEmployeeRestController.BankAccountRequestDTO();
            commercialAccountRequest.setAccountType("INVALID");
            commercialAccountRequest.setCurrency("USD");
            assertFalse(validator.validate(commercialAccountRequest).isEmpty());
        }
    }
}