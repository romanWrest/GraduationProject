package ru.dstu.dormitory.auth_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.auth_service.domain.model.Role;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByCode(RoleCode code);

    Set<Role> findAllByCodeIn(Set<RoleCode> codes);
}
