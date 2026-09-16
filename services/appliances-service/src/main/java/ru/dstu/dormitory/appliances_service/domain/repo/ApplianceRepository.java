package ru.dstu.dormitory.appliances_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, UUID>, JpaSpecificationExecutor<Appliance> {

    List<Appliance> findByResidentIdAndStatus(UUID residentId, ApplianceStatus status);

    List<Appliance> findByUserIdAndStatus(UUID userId, ApplianceStatus status);

    List<Appliance> findByRoomIdAndStatus(UUID roomId, ApplianceStatus status);

    @Query("""
            select coalesce(sum(a.powerWatts), 0) from Appliance a
            where a.roomId = :roomId and a.status = :status
            """)
    int sumPowerByRoomAndStatus(@Param("roomId") UUID roomId,
                                @Param("status") ApplianceStatus status);
}
