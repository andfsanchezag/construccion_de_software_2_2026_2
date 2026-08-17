package application.domain.ports.out;

import java.math.BigDecimal;

public interface BusinessConfigurationPort {

    BigDecimal getTransferApprovalThreshold();

    Integer getTransferApprovalExpirationMinutes();
}
