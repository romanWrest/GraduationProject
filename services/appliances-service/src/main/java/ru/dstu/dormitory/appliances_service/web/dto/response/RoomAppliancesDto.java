package ru.dstu.dormitory.appliances_service.web.dto.response;

import java.util.List;
import java.util.UUID;

public record RoomAppliancesDto(
        UUID roomId,
        int totalPowerWatts,
        int powerLimitWatts,
        List<ApplianceDto> appliances
) {
}
