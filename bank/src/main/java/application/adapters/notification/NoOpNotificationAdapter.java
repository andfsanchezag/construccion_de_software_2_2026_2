package application.adapters.notification;

import application.domain.enums.NotificationChannel;
import application.domain.models.Customer;
import application.domain.models.User;
import application.domain.ports.out.NotificationPort;
import org.springframework.stereotype.Component;

@Component
public class NoOpNotificationAdapter implements NotificationPort {

    @Override
    public void notifyUser(User user, String message, NotificationChannel channel) {
    }

    @Override
    public void notifyCustomer(Customer customer, String message, NotificationChannel channel) {
    }
}
