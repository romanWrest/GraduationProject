package ru.dstu.dormitory.reports_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.reports_service.domain.model.UserView;

import java.util.UUID;

@Repository
public interface UserViewRepository extends JpaRepository<UserView, UUID> {
}
