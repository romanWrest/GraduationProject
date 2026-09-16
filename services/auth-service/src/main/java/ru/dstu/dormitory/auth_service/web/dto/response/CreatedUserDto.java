package ru.dstu.dormitory.auth_service.web.dto.response;

public record CreatedUserDto(
        UserDto user,
        String temporaryPassword
) {
}
