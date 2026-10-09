package application.adapters.security.dtos;

import lombok.Data;

/**
 * DTO con el snapshot de User (y su Customer asociado, cuando existe)
 * que viaja como claims firmados dentro del JWT.
 *
 * <p>Es el contrato del adaptador de seguridad: el dominio nunca ve
 * claims/Mapeo JWT, solo el adaptador convierte User &lt;-&gt; este DTO
 * &lt;-&gt; claims del token.
 */
@Data
public class JwtClaimsDTO {

    private Integer version;
    private String username;
    private String userStatus;
    private String role;
    private String identification;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;

    private String customerType;
    private String customerIdentification;
    private String customerName;
    private String customerEmail;
    private String customerPhoneNumber;
    private String customerAddress;
    private String customerStatus;
    private String customerBirthDate;
    private String customerLegalRepresentativeIdentification;
}
