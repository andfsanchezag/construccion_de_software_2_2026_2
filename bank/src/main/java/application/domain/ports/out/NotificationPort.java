package application.domain.ports.out;

import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.enums.NotificationChannel;

public interface NotificationPort {

    void notifyUser(User user, String message, NotificationChannel channel);

    void notifyCustomer(Customer customer, String message, NotificationChannel channel);
}
