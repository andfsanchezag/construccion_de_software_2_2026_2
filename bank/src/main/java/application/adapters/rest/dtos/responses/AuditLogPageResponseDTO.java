package application.adapters.rest.dtos.responses;

import lombok.Data;

import java.util.List;

@Data
public class AuditLogPageResponseDTO {

    private List<AuditLogResponseDTO> content;
    private long totalElements;
    private int totalPages;
    private int pageNumber;
    private int pageSize;
}