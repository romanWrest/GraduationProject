package ru.dstu.dormitory.consumables_service.domain.repo;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsumableTypeRepository extends JpaRepository<ConsumableType, UUID> {

    /**
     * Получение типа расходника с pessimistic-lock.
     * Используется при изменении остатка (выдача, возврат, корректировка)
     * для защиты от гонок при параллельных операциях.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ConsumableType t where t.id = :id")
    Optional<ConsumableType> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByName(String name);
}
