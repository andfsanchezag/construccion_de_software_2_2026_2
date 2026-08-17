package application.domain.models;

import application.domain.valueobjects.CustomerStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Customer extends Person {
    private CustomerStatus status;
    // Populated on demand by ConsultCustomerProductsService; empty by default.
    private List<BankAccount> accounts = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();
    private List<Transfer> transfers = new ArrayList<>();
}
