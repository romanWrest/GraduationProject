package ru.dstu.dormitory.appliances_service.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.appliances_service.aspect.annotation.LogMethod;
import ru.dstu.dormitory.appliances_service.config.AppProperties;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceRepository;
import ru.dstu.dormitory.appliances_service.exception.RoomPowerLimitExceededException;
import ru.dstu.dormitory.appliances_service.service.RoomPowerService;
import ru.dstu.dormitory.appliances_service.util.LogPatterns;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomPowerServiceImpl implements RoomPowerService {

    private final ApplianceRepository applianceRepository;
    private final AppProperties appProperties;

    @Override
    @LogMethod
    @Transactional(readOnly = true)
    public int totalPowerWatts(UUID roomId) {
        if (roomId == null) {
            return 0;
        }
        int total = applianceRepository.sumPowerByRoomAndStatus(roomId, ApplianceStatus.APPROVED);
        List<Appliance> appliances = applianceRepository.findByRoomIdAndStatus(roomId, ApplianceStatus.APPROVED);
        log.info(LogPatterns.ROOM_POWER_CALCULATED, roomId, total, appliances.size());
        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appliance> approvedAppliancesInRoom(UUID roomId) {
        if (roomId == null) {
            return List.of();
        }
        return applianceRepository.findByRoomIdAndStatus(roomId, ApplianceStatus.APPROVED);
    }

    @Override
    public int powerLimitWatts() {
        return appProperties.getRoomPowerLimitWatts();
    }

    @Override
    public void requireWithinLimit(UUID roomId, int additionalWatts) {
        if (roomId == null) {
            return;
        }
        int current = applianceRepository.sumPowerByRoomAndStatus(roomId, ApplianceStatus.APPROVED);
        int projected = current + additionalWatts;
        int limit = powerLimitWatts();
        if (projected > limit) {
            throw new RoomPowerLimitExceededException(
                    "Превышен лимит мощности комнаты: roomId=%s, текущая=%d Вт, добавляется=%d Вт, лимит=%d Вт"
                            .formatted(roomId, current, additionalWatts, limit));
        }
    }
}
