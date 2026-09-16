package ru.dstu.dormitory.residents_service.domain.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.residents_service.domain.model.Room;

import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    boolean existsByNumber(String number);

    @Query("""
            select r from Room r
            where (:floor is null or r.floor = :floor)
              and (:capacity is null or r.capacity = :capacity)
              and (
                :hasFreeBeds is null
                or (
                  :hasFreeBeds = true
                  and r.capacity > (
                    select coalesce(count(res), 0)
                    from Resident res
                    where res.room = r and res.evictedAt is null
                  )
                )
                or (
                  :hasFreeBeds = false
                  and r.capacity <= (
                    select coalesce(count(res), 0)
                    from Resident res
                    where res.room = r and res.evictedAt is null
                  )
                )
              )
            """)
    Page<Room> search(@Param("floor") Short floor,
                      @Param("capacity") Short capacity,
                      @Param("hasFreeBeds") Boolean hasFreeBeds,
                      Pageable pageable);
}
