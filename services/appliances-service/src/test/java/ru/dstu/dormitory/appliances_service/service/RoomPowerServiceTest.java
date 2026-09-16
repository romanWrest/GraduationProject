package ru.dstu.dormitory.appliances_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.appliances_service.config.AppProperties;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.repo.ApplianceRepository;
import ru.dstu.dormitory.appliances_service.exception.RoomPowerLimitExceededException;
import ru.dstu.dormitory.appliances_service.service.Impl.RoomPowerServiceImpl;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomPowerServiceTest {

    @Mock
    private ApplianceRepository applianceRepository;
    @Mock
    private AppProperties appProperties;

    @InjectMocks
    private RoomPowerServiceImpl service;

    private UUID roomId;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        lenient().when(appProperties.getRoomPowerLimitWatts()).thenReturn(3500);
        lenient().when(applianceRepository.findByRoomIdAndStatus(any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("totalPowerWatts суммирует APPROVED-приборы")
    void totalPower() {
        when(applianceRepository.sumPowerByRoomAndStatus(roomId, ApplianceStatus.APPROVED)).thenReturn(2200);

        assertThat(service.totalPowerWatts(roomId)).isEqualTo(2200);
    }

    @Test
    @DisplayName("totalPowerWatts: roomId=null → 0")
    void totalPowerNullRoom() {
        assertThat(service.totalPowerWatts(null)).isZero();
    }

    @Test
    @DisplayName("requireWithinLimit: текущая+новая ≤ лимит — ОК")
    void withinLimitOk() {
        when(applianceRepository.sumPowerByRoomAndStatus(roomId, ApplianceStatus.APPROVED)).thenReturn(2000);

        assertThatCode(() -> service.requireWithinLimit(roomId, 1000))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireWithinLimit: текущая+новая > лимит → RoomPowerLimitExceededException")
    void exceedLimit() {
        when(applianceRepository.sumPowerByRoomAndStatus(roomId, ApplianceStatus.APPROVED)).thenReturn(3000);

        assertThatThrownBy(() -> service.requireWithinLimit(roomId, 1000))
                .isInstanceOf(RoomPowerLimitExceededException.class);
    }

    @Test
    @DisplayName("requireWithinLimit: ровно равно лимиту — ОК")
    void exactlyAtLimit() {
        when(applianceRepository.sumPowerByRoomAndStatus(roomId, ApplianceStatus.APPROVED)).thenReturn(2500);

        assertThatCode(() -> service.requireWithinLimit(roomId, 1000))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireWithinLimit: roomId=null — пропускает проверку")
    void nullRoomSkips() {
        assertThatCode(() -> service.requireWithinLimit(null, 5000))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("powerLimitWatts возвращает значение из конфигурации")
    void powerLimit() {
        assertThat(service.powerLimitWatts()).isEqualTo(3500);
    }
}
