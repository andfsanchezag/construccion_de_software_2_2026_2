package application.adapters.rest.dtos.responses;

import lombok.Data;

@Data
public class BusinessCustomerResponseDTO {

    private String identification;
    private String name;
    private String email;
    private String status;
    private String customerType;
    private LegalRepresentativeDTO legalRepresentative;

    @Data
    public static class LegalRepresentativeDTO {
        private String identification;
        private String name;
    }
}