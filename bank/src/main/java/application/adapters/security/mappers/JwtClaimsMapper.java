package application.adapters.security.mappers;

import application.adapters.security.dtos.JwtClaimsDTO;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.models.NaturalCustomer;
import application.domain.models.User;
import application.domain.valueobjects.CustomerStatus;
import application.domain.valueobjects.SystemRole;
import application.domain.valueobjects.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mapeo bidireccional entre el User del dominio y los claims JWT.
 *
 * <p>Toda la lógica de seguridad/validación de tokens que convierte
 * dominio &lt;-&gt; claims vive aquí, no en el adaptador ni en el dominio.
 */
public final class JwtClaimsMapper {

    private JwtClaimsMapper() {
    }

    public static JwtClaimsDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        JwtClaimsDTO dto = new JwtClaimsDTO();
        dto.setVersion(user.getAuthTokenVersion() != null ? user.getAuthTokenVersion() : 1);
        dto.setUsername(user.getUsername());
        dto.setUserStatus(user.getStatus() != null ? user.getStatus().getCode() : null);
        dto.setRole(user.getRole() != null ? user.getRole().getCode() : null);
        dto.setIdentification(user.getIdentification());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());

        Customer customer = user.getCustomer();
        if (customer != null) {
            dto.setCustomerType(customer instanceof BusinessCustomer ? "BUSINESS" : "NATURAL");
            dto.setCustomerIdentification(customer.getIdentification());
            dto.setCustomerName(customer.getName());
            dto.setCustomerEmail(customer.getEmail());
            dto.setCustomerPhoneNumber(customer.getPhoneNumber());
            dto.setCustomerAddress(customer.getAddress());
            dto.setCustomerStatus(customer.getStatus() != null ? customer.getStatus().getCode() : null);
            if (customer instanceof NaturalCustomer naturalCustomer) {
                dto.setCustomerBirthDate(
                        naturalCustomer.getBirthDate() != null ? naturalCustomer.getBirthDate().toString() : null);
            } else if (customer instanceof BusinessCustomer businessCustomer) {
                dto.setCustomerLegalRepresentativeIdentification(
                        businessCustomer.getLegalRepresentative() != null
                                ? businessCustomer.getLegalRepresentative().getIdentification()
                                : null);
            }
        }
        return dto;
    }

    public static Map<String, Object> toClaimsMap(User user) {
        return toClaimsMap(toDto(user));
    }

    public static Map<String, Object> toClaimsMap(JwtClaimsDTO dto) {
        Map<String, Object> claims = new LinkedHashMap<>();
        if (dto == null) {
            return claims;
        }
        claims.put("ver", dto.getVersion());
        claims.put("username", dto.getUsername());
        claims.put("status", dto.getUserStatus());
        claims.put("role", dto.getRole());
        claims.put("identification", dto.getIdentification());
        claims.put("name", dto.getName());
        claims.put("email", dto.getEmail());
        claims.put("phoneNumber", dto.getPhoneNumber());
        claims.put("address", dto.getAddress());
        claims.put("custType", dto.getCustomerType());
        claims.put("custIdentification", dto.getCustomerIdentification());
        claims.put("custName", dto.getCustomerName());
        claims.put("custEmail", dto.getCustomerEmail());
        claims.put("custPhoneNumber", dto.getCustomerPhoneNumber());
        claims.put("custAddress", dto.getCustomerAddress());
        claims.put("custStatus", dto.getCustomerStatus());
        claims.put("custBirthDate", dto.getCustomerBirthDate());
        claims.put("custLegalRepIdentification", dto.getCustomerLegalRepresentativeIdentification());
        return claims;
    }

    public static void applyClaims(JwtBuilder builder, User user) {
        toClaimsMap(user).forEach(builder::claim);
    }

    public static JwtClaimsDTO toDto(Claims claims) {
        if (claims == null) {
            return null;
        }
        JwtClaimsDTO dto = new JwtClaimsDTO();
        Object ver = claims.get("ver");
        if (ver instanceof Number number) {
            dto.setVersion(number.intValue());
        } else if (ver instanceof String text) {
            try {
                dto.setVersion(Integer.valueOf(text));
            } catch (NumberFormatException ignored) {
                dto.setVersion(null);
            }
        }
        dto.setUsername(claims.get("username", String.class));
        dto.setUserStatus(claims.get("status", String.class));
        dto.setRole(claims.get("role", String.class));
        dto.setIdentification(claims.get("identification", String.class));
        dto.setName(claims.get("name", String.class));
        dto.setEmail(claims.get("email", String.class));
        dto.setPhoneNumber(claims.get("phoneNumber", String.class));
        dto.setAddress(claims.get("address", String.class));
        dto.setCustomerType(claims.get("custType", String.class));
        dto.setCustomerIdentification(claims.get("custIdentification", String.class));
        dto.setCustomerName(claims.get("custName", String.class));
        dto.setCustomerEmail(claims.get("custEmail", String.class));
        dto.setCustomerPhoneNumber(claims.get("custPhoneNumber", String.class));
        dto.setCustomerAddress(claims.get("custAddress", String.class));
        dto.setCustomerStatus(claims.get("custStatus", String.class));
        dto.setCustomerBirthDate(claims.get("custBirthDate", String.class));
        dto.setCustomerLegalRepresentativeIdentification(
                claims.get("custLegalRepIdentification", String.class));
        return dto;
    }

    public static User toDomain(Claims claims) {
        return toDomain(toDto(claims), extractUserId(claims));
    }

    public static User toDomain(JwtClaimsDTO dto, Integer userId) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUserId(userId);
        user.setAuthTokenVersion(dto.getVersion());
        user.setUsername(dto.getUsername());
        user.setStatus(parseUserStatus(dto.getUserStatus()));
        user.setRole(dto.getRole() != null ? SystemRole.fromCode(dto.getRole()) : null);
        user.setIdentification(dto.getIdentification());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());

        if (dto.getCustomerType() != null) {
            Customer customer = "BUSINESS".equals(dto.getCustomerType())
                    ? new BusinessCustomer()
                    : new NaturalCustomer();
            customer.setIdentification(dto.getCustomerIdentification());
            customer.setName(dto.getCustomerName());
            customer.setEmail(dto.getCustomerEmail());
            customer.setPhoneNumber(dto.getCustomerPhoneNumber());
            customer.setAddress(dto.getCustomerAddress());
            customer.setStatus(parseCustomerStatus(dto.getCustomerStatus()));
            if (customer instanceof NaturalCustomer naturalCustomer) {
                if (dto.getCustomerBirthDate() != null) {
                    naturalCustomer.setBirthDate(LocalDate.parse(dto.getCustomerBirthDate()));
                }
            } else if (customer instanceof BusinessCustomer businessCustomer) {
                if (dto.getCustomerLegalRepresentativeIdentification() != null) {
                    NaturalCustomer legalRepresentative = new NaturalCustomer();
                    legalRepresentative.setIdentification(dto.getCustomerLegalRepresentativeIdentification());
                    businessCustomer.setLegalRepresentative(legalRepresentative);
                }
            }
            user.setCustomer(customer);
        }
        return user;
    }

    public static Integer extractUserId(Claims claims) {
        try {
            String subject = claims == null ? null : claims.getSubject();
            return subject == null ? null : Integer.valueOf(subject);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer extractTokenVersion(Claims claims) {
        try {
            Object ver = claims == null ? null : claims.get("ver");
            if (ver instanceof Number number) {
                return number.intValue();
            }
            if (ver instanceof String text) {
                return Integer.valueOf(text);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static UserStatus parseUserStatus(String code) {
        if (code == null) {
            return null;
        }
        if (UserStatus.ACTIVE.getCode().equals(code)) {
            return UserStatus.ACTIVE;
        }
        if (UserStatus.INACTIVE.getCode().equals(code)) {
            return UserStatus.INACTIVE;
        }
        if (UserStatus.BLOCKED.getCode().equals(code)) {
            return UserStatus.BLOCKED;
        }
        return null;
    }

    public static CustomerStatus parseCustomerStatus(String code) {
        if (code == null) {
            return null;
        }
        if (CustomerStatus.ACTIVE.getCode().equals(code)) {
            return CustomerStatus.ACTIVE;
        }
        if (CustomerStatus.INACTIVE.getCode().equals(code)) {
            return CustomerStatus.INACTIVE;
        }
        if (CustomerStatus.BLOCKED.getCode().equals(code)) {
            return CustomerStatus.BLOCKED;
        }
        return null;
    }
}
