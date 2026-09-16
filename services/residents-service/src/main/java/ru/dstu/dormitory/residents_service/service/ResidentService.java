package ru.dstu.dormitory.residents_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.web.dto.request.EnrollResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.EvictResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.MoveResidentRequest;
import ru.dstu.dormitory.residents_service.web.dto.request.UpdateResidentRequest;

import java.util.List;
import java.util.UUID;

public interface ResidentService {

    Resident enroll(EnrollResidentRequest request);

    Resident getById(UUID id);

    Resident getByUserId(UUID userId);

    Page<Resident> search(ResidentKind kind,
                          UUID roomId,
                          String faculty,
                          String search,
                          Boolean active,
                          Pageable pageable);

    Resident update(UUID id, UpdateResidentRequest request, boolean selfOnlyFields);

    Resident evict(UUID id, EvictResidentRequest request);

    Resident move(UUID id, MoveResidentRequest request);

    List<Resident> findActiveByRoom(UUID roomId);

    List<ResidencyHistory> getHistoryByResident(UUID residentId);

    List<ResidencyHistory> getHistoryByRoom(UUID roomId);

    List<Resident> findAllByIds(List<UUID> ids);
}
