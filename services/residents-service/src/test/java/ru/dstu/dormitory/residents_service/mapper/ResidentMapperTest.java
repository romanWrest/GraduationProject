package ru.dstu.dormitory.residents_service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidencyHistoryDto;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidentDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ResidentMapperTest {

    private final ResidentMapper mapper = Mappers.getMapper(ResidentMapper.class);

    @Test
    @DisplayName("toDto прокидывает roomId/roomNumber, если комната задана")
    void toDto_withRoom() {
        Room room = Room.builder().id(UUID.randomUUID()).number("404").floor((short) 4).capacity((short) 2).build();
        Resident resident = Resident.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .kind(ResidentKind.STUDENT)
                .faculty("ФИТ")
                .studyGroup("ИТ-31")
                .phone("+7000")
                .room(room)
                .enrolledAt(LocalDate.of(2026, 9, 1))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ResidentDto dto = mapper.toDto(resident);

        assertThat(dto.id()).isEqualTo(resident.getId());
        assertThat(dto.userId()).isEqualTo(resident.getUserId());
        assertThat(dto.kind()).isEqualTo(ResidentKind.STUDENT);
        assertThat(dto.faculty()).isEqualTo("ФИТ");
        assertThat(dto.studyGroup()).isEqualTo("ИТ-31");
        assertThat(dto.roomId()).isEqualTo(room.getId());
        assertThat(dto.roomNumber()).isEqualTo("404");
        assertThat(dto.enrolledAt()).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    @DisplayName("toDto с null room → roomId/roomNumber = null")
    void toDto_withoutRoom() {
        Resident resident = Resident.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .kind(ResidentKind.TEACHER)
                .department("Кафедра ИТ")
                .enrolledAt(LocalDate.now())
                .build();

        ResidentDto dto = mapper.toDto(resident);

        assertThat(dto.roomId()).isNull();
        assertThat(dto.roomNumber()).isNull();
        assertThat(dto.department()).isEqualTo("Кафедра ИТ");
    }

    @Test
    @DisplayName("toDtoList конвертирует коллекцию")
    void toDtoList_ok() {
        Resident r1 = Resident.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
                .kind(ResidentKind.STUDENT).enrolledAt(LocalDate.now()).build();
        Resident r2 = Resident.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
                .kind(ResidentKind.STUDENT).enrolledAt(LocalDate.now()).build();

        List<ResidentDto> list = mapper.toDtoList(List.of(r1, r2));

        assertThat(list).hasSize(2).extracting(ResidentDto::id).containsExactly(r1.getId(), r2.getId());
    }

    @Test
    @DisplayName("toHistoryDto раскрывает residentId/roomId/roomNumber")
    void toHistoryDto_ok() {
        Room room = Room.builder().id(UUID.randomUUID()).number("210").floor((short) 2).capacity((short) 3).build();
        Resident resident = Resident.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
                .kind(ResidentKind.STUDENT).enrolledAt(LocalDate.now()).build();
        ResidencyHistory history = ResidencyHistory.builder()
                .id(UUID.randomUUID())
                .resident(resident)
                .room(room)
                .movedInAt(LocalDate.of(2026, 1, 10))
                .movedOutAt(LocalDate.of(2026, 3, 1))
                .reason("переезд")
                .createdAt(Instant.now())
                .build();

        ResidencyHistoryDto dto = mapper.toHistoryDto(history);

        assertThat(dto.residentId()).isEqualTo(resident.getId());
        assertThat(dto.roomId()).isEqualTo(room.getId());
        assertThat(dto.roomNumber()).isEqualTo("210");
        assertThat(dto.movedInAt()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(dto.movedOutAt()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(dto.reason()).isEqualTo("переезд");
    }
}
