package application.adapters.rest.mappers;

import application.adapters.rest.dtos.requests.RegisterNaturalCustomerRequestDTO;
import application.adapters.rest.dtos.requests.RegisterBusinessCustomerRequestDTO;
import application.adapters.rest.dtos.requests.UpdateCustomerProfileRequestDTO;
import application.adapters.rest.dtos.responses.CustomerResponseDTO;
import application.adapters.rest.dtos.responses.BusinessCustomerResponseDTO;
import application.domain.models.NaturalCustomer;
import application.domain.models.BusinessCustomer;
import application.domain.models.Customer;
import application.domain.valueobjects.CustomerStatus;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

@UtilityClass
public class CustomerRestMapper {

    public NaturalCustomer toDomain(RegisterNaturalCustomerRequestDTO dto) {
        NaturalCustomer customer = new NaturalCustomer();
        customer.setIdentification(dto.getIdentification());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        if (dto.getBirthDate() != null) {
            customer.setBirthDate(dto.getBirthDate());
        }
        return customer;
    }

    public BusinessCustomer toDomain(RegisterBusinessCustomerRequestDTO dto) {
        BusinessCustomer customer = new BusinessCustomer();
        customer.setIdentification(dto.getIdentification());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setAddress(dto.getAddress());
        
        if (dto.getLegalRepresentativeIdentification() != null) {
            NaturalCustomer legalRep = new NaturalCustomer();
            legalRep.setIdentification(dto.getLegalRepresentativeIdentification());
            customer.setLegalRepresentative(legalRep);
        }
        return customer;
    }

    public void updateDomainFromDTO(UpdateCustomerProfileRequestDTO dto, Customer customer) {
        if (dto.getEmail() != null) {
            customer.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            customer.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getAddress() != null) {
            customer.setAddress(dto.getAddress());
        }
    }

    public CustomerResponseDTO toResponseDTO(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setIdentification(customer.getIdentification());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setStatus(customer.getStatus() != null ? customer.getStatus().getCode() : null);
        dto.setCustomerType(customer instanceof BusinessCustomer ? "BUSINESS" : "NATURAL");
        return dto;
    }

    public BusinessCustomerResponseDTO toBusinessResponseDTO(BusinessCustomer customer) {
        if (customer == null) {
            return null;
        }
        
        BusinessCustomerResponseDTO dto = new BusinessCustomerResponseDTO();
        dto.setIdentification(customer.getIdentification());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setStatus(customer.getStatus() != null ? customer.getStatus().getCode() : null);
        dto.setCustomerType("BUSINESS");
        
        if (customer.getLegalRepresentative() != null) {
            BusinessCustomerResponseDTO.LegalRepresentativeDTO repDto = new BusinessCustomerResponseDTO.LegalRepresentativeDTO();
            repDto.setIdentification(customer.getLegalRepresentative().getIdentification());
            repDto.setName(customer.getLegalRepresentative().getName());
            dto.setLegalRepresentative(repDto);
        }
        return dto;
    }
}