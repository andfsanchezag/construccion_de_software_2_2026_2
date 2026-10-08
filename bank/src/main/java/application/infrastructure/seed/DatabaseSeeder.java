package application.infrastructure.seed;

import application.domain.models.BankAccount;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.Loan;
import application.domain.models.NaturalCustomer;
import application.domain.models.Operation;
import application.domain.models.Transfer;
import application.domain.models.User;
import application.domain.ports.out.BankAccountRepositoryPort;
import application.domain.ports.out.CustomerRepositoryPort;
import application.domain.ports.out.LoanRepositoryPort;
import application.domain.ports.out.OperationRepositoryPort;
import application.domain.ports.out.PasswordServicePort;
import application.domain.ports.out.TransferRepositoryPort;
import application.domain.ports.out.UserRepositoryPort;
import application.domain.valueobjects.AccountType;
import application.domain.valueobjects.Currency;
import application.domain.valueobjects.LoanType;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OperationType;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.TransferStatus;
import application.domain.valueobjects.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Semillas para que el 100% de los endpoints funcionen al arrancar:
 * naturales (Oliver/Aria), empresa (Sanchez Rojas S.A.S.), todos los roles
 * (natural, business, operador, supervisor, teller, commercial, analyst),
 * cuentas, transferencias en EXECUTED/PENDING/WAITING y préstamos en
 * UNDER_REVIEW/APPROVED/DISBURSED.
 *
 * <p>Idempotente por identificador. Clave única para todos: Password123*.
 */
@Component
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final String PASSWORD = "Password123*";

    private final CustomerRepositoryPort customerRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final BankAccountRepositoryPort bankAccountRepositoryPort;
    private final TransferRepositoryPort transferRepositoryPort;
    private final OperationRepositoryPort operationRepositoryPort;
    private final LoanRepositoryPort loanRepositoryPort;
    private final PasswordServicePort passwordServicePort;

    public DatabaseSeeder(CustomerRepositoryPort customerRepositoryPort,
                          UserRepositoryPort userRepositoryPort,
                          BankAccountRepositoryPort bankAccountRepositoryPort,
                          TransferRepositoryPort transferRepositoryPort,
                          OperationRepositoryPort operationRepositoryPort,
                          LoanRepositoryPort loanRepositoryPort,
                          PasswordServicePort passwordServicePort) {
        this.customerRepositoryPort = customerRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.bankAccountRepositoryPort = bankAccountRepositoryPort;
        this.transferRepositoryPort = transferRepositoryPort;
        this.operationRepositoryPort = operationRepositoryPort;
        this.loanRepositoryPort = loanRepositoryPort;
        this.passwordServicePort = passwordServicePort;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Iniciando semillas de base de datos (todos los roles y estados)...");
        try {
            // ---- 1. Naturales ----
            NaturalCustomer oliver = seedNaturalCustomer(
                    "1001234567", "Oliver Sanchez Rojas", "oliver.sanchez@banco.com",
                    "3001001001", "Calle 10 #20-30 Medellin", LocalDate.of(1990, 5, 15));
            NaturalCustomer aria = seedNaturalCustomer(
                    "1007654321", "Aria Sanchez Rojas", "aria.sanchez@banco.com",
                    "3002002002", "Carrera 45 #30-12 Medellin", LocalDate.of(1995, 9, 20));

            User oliverUser = seedCustomerUser("oliver.sanchez", SystemRole.NATURAL_CUSTOMER, oliver);
            User ariaUser = seedCustomerUser("aria.sanchez", SystemRole.NATURAL_CUSTOMER, aria);

            // ---- 2. Empresa + cuentas ----
            BusinessCustomer empresa = seedBusinessCustomer(
                    "900123456", "Sanchez Rojas S.A.S.", "contacto@sanchezrojas.com",
                    "6041002003", "Av. El Poblado #45-100 Medellin", oliver);
            User empresaAdmin = seedCustomerUser("empresa.admin", SystemRole.BUSINESS_CUSTOMER, empresa);
            User operador = seedCustomerUser("empresa.operador", SystemRole.BUSINESS_OPERATOR, empresa);
            User supervisor = seedCustomerUser("empresa.supervisor", SystemRole.BUSINESS_SUPERVISOR, empresa);

            // ---- 3. Empleados sin customer ----
            User teller = seedEmployee("teller.maria", SystemRole.TELLER_EMPLOYEE,
                    "T-1001", "Maria Teller", "maria.teller@banco.com");
            User commercial = seedEmployee("commercial.carlos", SystemRole.COMMERCIAL_EMPLOYEE,
                    "C-2001", "Carlos Commercial", "carlos.commercial@banco.com");
            User analyst = seedEmployee("analyst.laura", SystemRole.INTERNAL_ANALYST,
                    "A-3001", "Laura Analyst", "laura.analyst@banco.com");

            // ---- 4. Cuentas ----
            seedAccount("BA-OLIVER-AHORROS", AccountType.SAVINGS, oliver,
                    new BigDecimal("5000000.00"), oliverUser);
            seedAccount("BA-OLIVER-CORRIENTE", AccountType.CHECKING, oliver,
                    new BigDecimal("2000000.00"), oliverUser);
            seedAccount("BA-ARIA-AHORROS", AccountType.SAVINGS, aria,
                    new BigDecimal("3000000.00"), ariaUser);
            seedAccount("BA-ARIA-CORRIENTE", AccountType.CHECKING, aria,
                    new BigDecimal("1500000.00"), ariaUser);
            seedAccount("BA-EMPRESA-PRINCIPAL", AccountType.BUSINESS, empresa,
                    new BigDecimal("20000000.00"), empresaAdmin);
            seedAccount("BA-EMPRESA-OPERATIVA", AccountType.BUSINESS, empresa,
                    new BigDecimal("10000000.00"), empresaAdmin);
            // Cuenta en cero para probar close sin 409.
            seedAccount("BA-CIERRE-CERO", AccountType.SAVINGS, oliver,
                    BigDecimal.ZERO, oliverUser);

            BankAccount oliverSavings = requireAccount("BA-OLIVER-AHORROS");
            BankAccount ariaSavings = requireAccount("BA-ARIA-AHORROS");
            BankAccount oliverChecking = requireAccount("BA-OLIVER-CORRIENTE");
            BankAccount ariaChecking = requireAccount("BA-ARIA-CORRIENTE");
            BankAccount empresaPrincipal = requireAccount("BA-EMPRESA-PRINCIPAL");
            BankAccount empresaOperativa = requireAccount("BA-EMPRESA-OPERATIVA");

            // ---- 5. Transferencias naturales EXECUTED ----
            seedExecutedTransfer("TR-SEED-001", oliverSavings, ariaSavings, new BigDecimal("500000.00"), oliverUser);
            seedExecutedTransfer("TR-SEED-002", ariaSavings, oliverSavings, new BigDecimal("200000.00"), ariaUser);
            seedExecutedTransfer("TR-SEED-003", oliverChecking, ariaChecking, new BigDecimal("150000.00"), oliverUser);
            seedExecutedTransfer("TR-SEED-004", ariaChecking, oliverChecking, new BigDecimal("75000.00"), ariaUser);
            seedExecutedTransfer("TR-SEED-005", oliverSavings, ariaChecking, new BigDecimal("300000.00"), oliverUser);
            seedExecutedTransfer("TR-SEED-006", ariaSavings, oliverChecking, new BigDecimal("120000.00"), ariaUser);

            // ---- 6. Transferencias empresa: PENDING + WAITING_FOR_APPROVAL ----
            seedPendingTransfer("TR-SEED-PEND-001", empresaPrincipal, empresaOperativa,
                    new BigDecimal("500000.00"), operador);
            seedWaitingTransfer("TR-SEED-WAIT-001", empresaPrincipal, empresaOperativa,
                    new BigDecimal("12000000.00"), operador);
            seedWaitingTransfer("TR-SEED-WAIT-002", empresaOperativa, empresaPrincipal,
                    new BigDecimal("11000000.00"), operador);

            // ---- 7. Préstamos en los 3 estados ----
            seedLoanReview("LN-SEED-REVIEW-001", oliver, oliverSavings,
                    LoanType.PERSONAL, new BigDecimal("1000000.00"), 24, oliverUser);
            seedLoanApproved("LN-SEED-APPROVED-001", aria, ariaSavings,
                    LoanType.PERSONAL, new BigDecimal("1000000.00"), new BigDecimal("900000.00"),
                    new BigDecimal("0.1200"), 24, ariaUser);
            seedLoanDisbursed("LN-SEED-DISBURSED-001", oliver, oliverSavings,
                    LoanType.PERSONAL, new BigDecimal("2000000.00"), new BigDecimal("1800000.00"),
                    new BigDecimal("0.1000"), 36, oliverUser);

            log.info("Semillas OK: naturales Oliver(1001234567)/Aria(1007654321), empresa 900123456, "
                    + "usuarios oliver.sanchez/aria.sanchez/empresa.admin/empresa.operador/empresa.supervisor/"
                    + "teller.maria/commercial.carlos/analyst.laura (clave {}), "
                    + "7 cuentas, 9 transferencias (6 EXECUTED + 1 PENDING + 2 WAITING), "
                    + "3 préstamos (UNDER_REVIEW/APPROVED/DISBURSED).",
                    PASSWORD);
        } catch (Exception ex) {
            log.error("Fallo en semillas: {}: {}. La app continúa.",
                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
        }
    }

    // ================= Naturales / empresa =================

    private NaturalCustomer seedNaturalCustomer(String identification, String name, String email,
                                                String phone, String address, LocalDate birthDate) {
        NaturalCustomer probe = new NaturalCustomer();
        probe.setIdentification(identification);
        if (customerRepositoryPort.existsByIdentification(probe)) {
            Customer stored = customerRepositoryPort.findByIdentification(probe).orElse(null);
            log.info("Customer '{}' ({}) ya existe: se omite.", name, identification);
            if (stored instanceof NaturalCustomer natural) {
                return natural;
            }
            NaturalCustomer fallback = new NaturalCustomer();
            fallback.setIdentification(identification);
            return fallback;
        }
        NaturalCustomer customer = new NaturalCustomer();
        customer.setIdentification(identification);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        customer.setAddress(address);
        customer.setRole(SystemRole.NATURAL_CUSTOMER);
        customer.setBirthDate(birthDate);
        customer.register();
        Customer saved = customerRepositoryPort.save(customer);
        log.info("Customer natural creado: '{}' id='{}'.", name, identification);
        registerOperation(null, saved, OperationType.CUSTOMER_REGISTRATION);
        return (NaturalCustomer) saved;
    }

    private BusinessCustomer seedBusinessCustomer(String nit, String name, String email,
                                                  String phone, String address, NaturalCustomer rep) {
        BusinessCustomer probe = new BusinessCustomer();
        probe.setIdentification(nit);
        if (customerRepositoryPort.existsByIdentification(probe)) {
            log.info("Empresa '{}' ({}) ya existe: se omite.", name, nit);
            Customer stored = customerRepositoryPort.findByIdentification(probe).orElse(null);
            if (stored instanceof BusinessCustomer business) {
                return business;
            }
            BusinessCustomer fallback = new BusinessCustomer();
            fallback.setIdentification(nit);
            return fallback;
        }
        BusinessCustomer customer = new BusinessCustomer();
        customer.setIdentification(nit);
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        customer.setAddress(address);
        customer.setRole(SystemRole.BUSINESS_CUSTOMER);
        NaturalCustomer repRef = new NaturalCustomer();
        repRef.setIdentification(rep.getIdentification());
        customer.setLegalRepresentative(repRef);
        customer.register();
        Customer saved = customerRepositoryPort.save(customer);
        log.info("Empresa creada: '{}' NIT='{}' rep='{}'.", name, nit, rep.getIdentification());
        registerOperation(null, saved, OperationType.CUSTOMER_REGISTRATION);
        return (BusinessCustomer) saved;
    }

    private User seedCustomerUser(String username, SystemRole role, Customer customer) {
        User probe = new User();
        probe.setUsername(username);
        if (userRepositoryPort.existsByUsername(probe)) {
            log.info("Usuario '{}' ya existe: se omite.", username);
            return userRepositoryPort.findByUsername(probe).orElse(probe);
        }
        Customer authoritative = customerRepositoryPort.findByIdentification(customer).orElse(customer);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordServicePort.encrypt(PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        user.setIdentification(authoritative.getIdentification());
        user.setName(authoritative.getName());
        user.setEmail(authoritative.getEmail());
        user.setPhoneNumber(authoritative.getPhoneNumber());
        user.setAddress(authoritative.getAddress());
        user.setCustomer(authoritative);
        user.setAuthTokenVersion(1);
        User saved = userRepositoryPort.save(user);
        log.info("Usuario customer creado: '{}' role='{}' customer='{}'.", username, role.getCode(),
                authoritative.getIdentification());
        return saved;
    }

    private User seedEmployee(String username, SystemRole role, String identification,
                              String name, String email) {
        User probe = new User();
        probe.setUsername(username);
        if (userRepositoryPort.existsByUsername(probe)) {
            log.info("Empleado '{}' ya existe: se omite.", username);
            return userRepositoryPort.findByUsername(probe).orElse(probe);
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordServicePort.encrypt(PASSWORD));
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(role);
        user.setIdentification(identification);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNumber("6040000000");
        user.setAddress("Oficina Principal Medellin");
        user.setCustomer(null);
        user.setAuthTokenVersion(1);
        User saved = userRepositoryPort.save(user);
        log.info("Empleado creado: '{}' role='{}'.", username, role.getCode());
        return saved;
    }

    // ================= Cuentas / transferencias / préstamos =================

    private void seedAccount(String identifier, AccountType type, Customer owner,
                             BigDecimal initialBalance, User createdBy) {
        BankAccount probe = new BankAccount();
        probe.setIdentifier(identifier);
        if (bankAccountRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Cuenta '{}' ya existe: se omite.", identifier);
            return;
        }
        Customer authoritative = customerRepositoryPort.findByIdentification(owner).orElse(owner);
        BankAccount account = new BankAccount();
        account.setIdentifier(identifier);
        account.setAccountType(type);
        account.setOwner(authoritative);
        account.setCurrency(Currency.COP);
        account.setCurrentBalance(BigDecimal.ZERO);
        account.open(LocalDate.now());
        if (initialBalance != null && initialBalance.signum() > 0) {
            account.deposit(Money.of(initialBalance, Currency.COP));
        }
        BankAccount saved = bankAccountRepositoryPort.save(account);
        log.info("Cuenta creada: '{}' owner='{}' balance='{} COP'.", identifier,
                authoritative.getIdentification(), initialBalance);
        registerOperation(createdBy, saved, OperationType.ACCOUNT_OPENING);
        if (initialBalance != null && initialBalance.signum() > 0) {
            registerOperation(createdBy, saved, OperationType.DEPOSIT);
        }
    }

    private BankAccount requireAccount(String identifier) {
        BankAccount probe = new BankAccount();
        probe.setIdentifier(identifier);
        return bankAccountRepositoryPort.findByIdentifier(probe)
                .orElseThrow(() -> new IllegalStateException("Cuenta semilla no encontrada: '" + identifier + "'."));
    }

    private void seedExecutedTransfer(String id, BankAccount source, BankAccount destination,
                                      BigDecimal amount, User createdBy) {
        Transfer probe = new Transfer();
        probe.setIdentifier(id);
        if (transferRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Transferencia '{}' ya existe: se omite.", id);
            return;
        }
        BankAccount src = requireAccount(source.getIdentifier());
        BankAccount dst = requireAccount(destination.getIdentifier());
        src.withdraw(Money.of(amount, Currency.COP));
        dst.deposit(Money.of(amount, Currency.COP));
        bankAccountRepositoryPort.update(src);
        bankAccountRepositoryPort.update(dst);
        Transfer transfer = new Transfer();
        transfer.setIdentifier(id);
        transfer.setSourceAccount(src);
        transfer.setDestinationAccount(dst);
        transfer.setAmount(amount);
        transfer.setCreationDate(LocalDateTime.now().minusDays(1));
        transfer.setCreatedBy(createdBy);
        transfer.assignInitialStatus(false);
        transfer.markExecuted();
        transfer.setApprovalDate(LocalDateTime.now().minusDays(1));
        transfer.setApprovedBy(createdBy);
        transferRepositoryPort.save(transfer);
        log.info("Transferencia EXECUTED '{}': {} -> {} {} COP.", id, src.getIdentifier(), dst.getIdentifier(), amount);
        registerOperation(createdBy, transfer, OperationType.TRANSFER_CREATION);
        registerOperation(createdBy, transfer, OperationType.TRANSFER_EXECUTION);
    }

    private void seedPendingTransfer(String id, BankAccount source, BankAccount destination,
                                     BigDecimal amount, User createdBy) {
        Transfer probe = new Transfer();
        probe.setIdentifier(id);
        if (transferRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Transferencia '{}' ya existe: se omite.", id);
            return;
        }
        BankAccount src = requireAccount(source.getIdentifier());
        BankAccount dst = requireAccount(destination.getIdentifier());
        Transfer transfer = new Transfer();
        transfer.setIdentifier(id);
        transfer.setSourceAccount(src);
        transfer.setDestinationAccount(dst);
        transfer.setAmount(amount);
        transfer.setCreationDate(LocalDateTime.now());
        transfer.setCreatedBy(createdBy);
        try {
            java.lang.reflect.Field f = Transfer.class.getDeclaredField("transferStatus");
            f.setAccessible(true);
            f.set(transfer, TransferStatus.PENDING);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("No se pudo sembrar PENDING", ex);
        }
        transferRepositoryPort.save(transfer);
        log.info("Transferencia PENDING '{}': {} -> {} {} COP (para submit/approve/reject).",
                id, src.getIdentifier(), dst.getIdentifier(), amount);
        registerOperation(createdBy, transfer, OperationType.TRANSFER_CREATION);
    }

    private void seedWaitingTransfer(String id, BankAccount source, BankAccount destination,
                                     BigDecimal amount, User createdBy) {
        Transfer probe = new Transfer();
        probe.setIdentifier(id);
        if (transferRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Transferencia '{}' ya existe: se omite.", id);
            return;
        }
        BankAccount src = requireAccount(source.getIdentifier());
        BankAccount dst = requireAccount(destination.getIdentifier());
        Transfer transfer = new Transfer();
        transfer.setIdentifier(id);
        transfer.setSourceAccount(src);
        transfer.setDestinationAccount(dst);
        transfer.setAmount(amount);
        transfer.setCreationDate(LocalDateTime.now());
        transfer.setCreatedBy(createdBy);
        transfer.markWaitingForApproval();
        transferRepositoryPort.save(transfer);
        log.info("Transferencia WAITING_FOR_APPROVAL '{}': {} -> {} {} COP (para supervisor approve/reject).",
                id, src.getIdentifier(), dst.getIdentifier(), amount);
        registerOperation(createdBy, transfer, OperationType.TRANSFER_CREATION);
    }

    private void seedLoanReview(String id, Customer applicant, BankAccount dest,
                                LoanType type, BigDecimal requested, int term, User createdBy) {
        Loan probe = new Loan();
        probe.setIdentifier(id);
        if (loanRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Préstamo '{}' ya existe: se omite.", id);
            return;
        }
        Loan loan = new Loan();
        loan.setIdentifier(id);
        loan.setApplicant(customerRepositoryPort.findByIdentification(applicant).orElse(applicant));
        loan.setLoanType(type);
        loan.setRequestedAmount(requested);
        loan.setTermInMonths(term);
        loan.setCurrency(Currency.COP);
        BankAccount d = requireAccount(dest.getIdentifier());
        loan.setDestinationAccount(d);
        loan.submitForReview();
        loanRepositoryPort.save(loan);
        log.info("Préstamo UNDER_REVIEW '{}': applicant='{}' {} COP.", id,
                applicant.getIdentification(), requested);
        registerOperation(createdBy, loan, OperationType.LOAN_APPLICATION);
    }

    private void seedLoanApproved(String id, Customer applicant, BankAccount dest,
                                  LoanType type, BigDecimal requested, BigDecimal approved,
                                  BigDecimal rate, int term, User createdBy) {
        Loan probe = new Loan();
        probe.setIdentifier(id);
        if (loanRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Préstamo '{}' ya existe: se omite.", id);
            return;
        }
        seedLoanReview(id, applicant, dest, type, requested, term, createdBy);
        Loan stored = loanRepositoryPort.findByIdentifier(probe).orElseThrow();
        stored.approve(approved, rate);
        loanRepositoryPort.update(stored);
        log.info("Préstamo APPROVED '{}': approved='{}' rate='{}'.", id, approved, rate);
        registerOperation(createdBy, stored, OperationType.LOAN_APPROVAL);
    }

    private void seedLoanDisbursed(String id, Customer applicant, BankAccount dest,
                                   LoanType type, BigDecimal requested, BigDecimal approved,
                                   BigDecimal rate, int term, User createdBy) {
        Loan probe = new Loan();
        probe.setIdentifier(id);
        if (loanRepositoryPort.findByIdentifier(probe).isPresent()) {
            log.info("Préstamo '{}' ya existe: se omite.", id);
            return;
        }
        seedLoanApproved(id, applicant, dest, type, requested, approved, rate, term, createdBy);
        Loan stored = loanRepositoryPort.findByIdentifier(probe).orElseThrow();
        BankAccount d = requireAccount(dest.getIdentifier());
        d.deposit(Money.of(approved, Currency.COP));
        bankAccountRepositoryPort.update(d);
        stored.disburse();
        loanRepositoryPort.update(stored);
        log.info("Préstamo DISBURSED '{}': desembolsado '{}' a '{}' (habilita payments).",
                id, approved, d.getIdentifier());
        registerOperation(createdBy, stored, OperationType.LOAN_DISBURSEMENT);
    }

    private void registerOperation(User performedBy, Object product, OperationType type) {
        try {
            Operation op = new Operation();
            op.setOperationType(type);
            op.setExecutionDate(LocalDateTime.now());
            op.setPerformedBy(performedBy);
            if (product instanceof BankAccount account) {
                op.setAffectedProduct(account);
            } else if (product instanceof Transfer transfer) {
                op.setAffectedProduct(transfer);
            } else if (product instanceof Loan loan) {
                op.setAffectedProduct(loan);
            } else if (product instanceof Customer) {
                op.setAffectedProduct(null);
                operationRepositoryPort.save(op);
                return;
            }
            operationRepositoryPort.save(op);
        } catch (Exception ex) {
            log.warn("No se pudo registrar operación '{}': {}", type.getCode(), ex.getMessage());
        }
    }
}
