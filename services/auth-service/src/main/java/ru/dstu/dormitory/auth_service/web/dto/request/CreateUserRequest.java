package ru.dstu.dormitory.auth_service.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.util.Set;

public record CreateUserRequest(
        @NotBlank(message = "Поле электронной почты не должно быть пустым")
        @Email(message = "Введите корректный формат электронной почты") String email,
        @NotBlank(message = "ФИО не должно быть пустым")
        String fullName,

        //TODO сделать аннотацию для валидации номера телефона

        String phone,
        @NotEmpty Set<RoleCode> roles
) {
}
