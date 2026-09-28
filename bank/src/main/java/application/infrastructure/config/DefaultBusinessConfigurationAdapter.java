package application.infrastructure.config;

import application.domain.ports.out.BusinessConfigurationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DefaultBusinessConfigurationAdapter implements BusinessConfigurationPort {

    private final BigDecimal transferApprovalThreshold;
    private final Integer transferApprovalExpirationMinutes;

    public DefaultBusinessConfigurationAdapter(
            @Value("${business.transfer.approval-threshold:10000000}") BigDecimal transferApprovalThreshold,
            @Value("${business.transfer.approval-expiration-minutes:60}") Integer transferApprovalExpirationMinutes) {
        this.transferApprovalThreshold = transferApprovalThreshold;
        this.transferApprovalExpirationMinutes = transferApprovalExpirationMinutes;
    }

    @Override
    public BigDecimal getTransferApprovalThreshold() {
        return transferApprovalThreshold;
    }

    @Override
    public Integer getTransferApprovalExpirationMinutes() {
        return transferApprovalExpirationMinutes;
    }
}
