package ru.dstu.dormitory.auth_service.web.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private String error;
    private String message;
    private String description;
    private String path;
    private String details;
    private Map<String, String> validationErrors;
}