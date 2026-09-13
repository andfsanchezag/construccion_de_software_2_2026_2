package application.domain.services.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import application.domain.exceptions.InvalidCustomerException;
import application.domain.exceptions.InvalidCustomerStatusException;
import application.domain.exceptions.InvalidLegalRepresentativeException;
import application.domain.models.BusinessCustomer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;

class CustomerDomainModelTest {

    @Test
    void adultNaturalCustomerRegistersWithActiveStatus() {
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("7001");

        customer.register();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertTrue(customer.isAdult());
    }

    @Test
    void underageNaturalCustomerCannotRegister() {
        NaturalCustomer customer = CustomerTestSupport.naturalCustomer(
                "7002", LocalDate.now().minusYears(17), null);

        assertFalse(customer.isAdult());
        assertThrows(InvalidCustomerException.class, customer::register);
    }

    @Test
    void naturalCustomerWithoutBirthDateCannotRegister() {
        NaturalCustomer customer = CustomerTestSupport.registrationCandidate("7003");
        customer.setBirthDate(null);

        assertThrows(InvalidCustomerException.class, customer::register);
    }

    @Test
    void alreadyRegisteredCustomerCannotRegisterAgain() {
        NaturalCustomer customer = CustomerTestSupport.adultCustomer("7004");

        assertThrows(InvalidCustomerException.class, customer::register);
    }

    @Test
    void customerStatusTransitionsFollowTheDefinedMatrix() {
        NaturalCustomer active = CustomerTestSupport.adultCustomer("7005");
        active.changeStatus(CustomerStatus.BLOCKED);
        assertEquals(CustomerStatus.BLOCKED, active.getStatus());
        active.changeStatus(CustomerStatus.ACTIVE);
        assertEquals(CustomerStatus.ACTIVE, active.getStatus());
        active.changeStatus(CustomerStatus.INACTIVE);
        assertEquals(CustomerStatus.INACTIVE, active.getStatus());
        active.changeStatus(CustomerStatus.ACTIVE);
        assertEquals(CustomerStatus.ACTIVE, active.getStatus());
    }

    @Test
    void invalidStatusTransitionsAreRejected() {
        NaturalCustomer active = CustomerTestSupport.adultCustomer("7006");

        assertThrows(InvalidCustomerStatusException.class,
                () -> active.changeStatus(CustomerStatus.ACTIVE));

        NaturalCustomer blocked = CustomerTestSupport.adultCustomer("7007");
        blocked.setStatus(CustomerStatus.BLOCKED);
        assertThrows(InvalidCustomerStatusException.class,
                () -> blocked.changeStatus(CustomerStatus.INACTIVE));

        NaturalCustomer activeAgain = CustomerTestSupport.adultCustomer("7008");
        assertThrows(InvalidCustomerStatusException.class,
                () -> activeAgain.changeStatus(null));
    }

    @Test
    void businessCustomerRequiresLegalRepresentativeToRegister() {
        BusinessCustomer customer = CustomerTestSupport.businessCustomer("7009", null);

        assertThrows(InvalidLegalRepresentativeException.class, customer::register);
    }

    @Test
    void businessCustomerWithRepresentativeRegisters() {
        BusinessCustomer customer = CustomerTestSupport.businessCustomer(
                "7010", CustomerTestSupport.adultCustomer("7011"));

        customer.register();

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    void updateFromAppliesOnlyMutableFields() {
        NaturalCustomer persisted = CustomerTestSupport.adultCustomer("7012");
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("7012");
        desired.setName("New Name");
        desired.setStatus(CustomerStatus.BLOCKED);

        persisted.updateFrom(desired);

        assertEquals("New Name", persisted.getName());
        assertEquals(CustomerStatus.ACTIVE, persisted.getStatus());
    }

    @Test
    void updateFromRejectsIdentificationChanges() {
        NaturalCustomer persisted = CustomerTestSupport.adultCustomer("7013");
        NaturalCustomer desired = CustomerTestSupport.adultCustomer("7014");

        assertThrows(InvalidCustomerException.class, () -> persisted.updateFrom(desired));
    }

    @Test
    void customerStatusAndUserStatusAreIndependent() {
        NaturalCustomer customer = CustomerTestSupport.adultCustomer("7015");
        User user = new User();
        user.setUsername("user7015");
        user.setRole(SystemRole.NATURAL_CUSTOMER);
        user.setStatus(UserStatus.ACTIVE);
        user.setCustomer(customer);

        customer.changeStatus(CustomerStatus.BLOCKED);

        assertEquals(CustomerStatus.BLOCKED, customer.getStatus());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
}