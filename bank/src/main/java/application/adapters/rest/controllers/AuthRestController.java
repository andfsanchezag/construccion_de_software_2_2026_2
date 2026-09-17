package application.adapters.rest.controllers;

import application.adapters.rest.dtos.requests.LoginRequestDTO;
import application.adapters.rest.dtos.requests.RegisterNaturalCustomerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterBusinessCustomerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterUserRequestDTO;
import application.adapters.rest.dtos.responses.LoginResponseDTO;
import application.adapters.rest.dtos.responses.CustomerResponseDTO;
import application.adapters.rest.dtos.responses.BusinessCustomerResponseDTO;
import application.adapters.rest.dtos.responses.UserResponseDTO;
import application.adapters.rest.mappers.UserRestMapper;
import application.adapters.rest.mappers.CustomerRestMapper;
import application.domain.models.NaturalCustomer;
import application.domain.models.BusinessCustomer;
import application.domain.models.User;
import application.domain.ports.in.PublicAccessPort;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final PublicAccessPort publicAccessPort;

    public AuthRestController(PublicAccessPort publicAccessPort) {
        this.publicAccessPort = publicAccessPort;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        User user = UserRestMapper.toDomain(requestDTO);
        String token = publicAccessPort.login(user);
        
        // The user returned from login has the JWT generated, but we need to get the full user
        // For now, we'll use the user from the login request with the token
        LoginResponseDTO response = UserRestMapper.toResponseDTO(token, user, 3600000L);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        publicAccessPort.logout(null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register/natural-customer")
    public ResponseEntity<CustomerResponseDTO> registerNaturalCustomer(@Valid @RequestBody RegisterNaturalCustomerRequestDTO requestDTO) {
        NaturalCustomer customer = CustomerRestMapper.toDomain(requestDTO);
        NaturalCustomer saved = publicAccessPort.registerNaturalCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerRestMapper.toResponseDTO(saved));
    }

    @PostMapping("/register/business-customer")
    public ResponseEntity<BusinessCustomerResponseDTO> registerBusinessCustomer(@Valid @RequestBody RegisterBusinessCustomerRequestDTO requestDTO) {
        BusinessCustomer customer = CustomerRestMapper.toDomain(requestDTO);
        BusinessCustomer saved = publicAccessPort.registerBusinessCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerRestMapper.toBusinessResponseDTO(saved));
    }

    @PostMapping("/register/user")
    public ResponseEntity<UserResponseDTO> registerCustomerUser(@Valid @RequestBody RegisterUserRequestDTO requestDTO) {
        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setPassword(requestDTO.getPassword());
        
        if (requestDTO.getRole() != null) {
            user.setRole(mapSystemRole(requestDTO.getRole()));
        }
        
        // The customer identification needs to be set on the user
        application.domain.models.Customer customer = new application.domain.models.NaturalCustomer();
        customer.setIdentification(requestDTO.getCustomerIdentification());
        user.setCustomer(customer);
        
        User saved = publicAccessPort.registerCustomerUser(user);
        
        UserResponseDTO response = new UserResponseDTO();
        response.setUserId(saved.getUserId() != null ? String.valueOf(saved.getUserId()) : null);
        response.setUsername(saved.getUsername());
        response.setRole(saved.getRole() != null ? saved.getRole().getCode() : null);
        response.setStatus(saved.getStatus() != null ? saved.getStatus().getCode() : null);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private application.domain.valueobjects.SystemRole mapSystemRole(String code) {
        if (code == null) {
            return null;
        }
        switch (code) {
            case "NATURAL_CUSTOMER":
                return application.domain.valueobjects.SystemRole.NATURAL_CUSTOMER;
            case "BUSINESS_CUSTOMER":
                return application.domain.valueobjects.SystemRole.BUSINESS_CUSTOMER;
            case "TELLER_EMPLOYEE":
                return application.domain.valueobjects.SystemRole.TELLER_EMPLOYEE;
            case "COMMERCIAL_EMPLOYEE":
                return application.domain.valueobjects.SystemRole.COMMERCIAL_EMPLOYEE;
            case "BUSINESS_OPERATOR":
                return application.domain.valueobjects.SystemRole.BUSINESS_OPERATOR;
            case "BUSINESS_SUPERVISOR":
                return application.domain.valueobjects.SystemRole.BUSINESS_SUPERVISOR;
            case "INTERNAL_ANALYST":
                return application.domain.valueobjects.SystemRole.INTERNAL_ANALYST;
            default:
                return null;
        }
    }
}