package ru.dstu.dormitory.auth_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.dstu.dormitory.auth_service.domain.model.Role;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;
import ru.dstu.dormitory.auth_service.web.dto.response.UserDto;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToCodes")
    @Mapping(target = "active", source = "active")
    UserDto toDto(User user);

    @Named("rolesToCodes")
    default Set<RoleCode> rolesToCodes(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream().map(Role::getCode).collect(Collectors.toUnmodifiableSet());
    }
}
