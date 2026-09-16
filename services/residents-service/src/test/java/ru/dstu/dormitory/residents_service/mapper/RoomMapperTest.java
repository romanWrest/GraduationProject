package ru.dstu.dormitory.residents_service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.response.RoomDto;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoomMapperTest {

    private final RoomMapper mapper = Mappers.getMapper(RoomMapper.class);

    @Test
    @DisplayName("fromCreate создаёт Room без id/временных меток")
    void fromCreate_ok() {
        CreateRoomRequest req = new CreateRoomRequest("201", (short) 2, (short) 3, "две кровати");
        Room room = mapper.fromCreate(req);

        assertThat(room.getId()).isNull();
        assertThat(room.getNumber()).isEqualTo("201");
        assertThat(room.getFloor()).isEqualTo((short) 2);
        assertThat(room.getCapacity()).isEqualTo((short) 3);
        assertThat(room.getNotes()).isEqualTo("две кровати");
        assertThat(room.getCreatedAt()).isNull();
        assertThat(room.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("toDto прокидывает поля и проставляет occupied")
    void toDto_ok() {
        Room room = Room.builder()
                .id(UUID.randomUUID())
                .number("305")
                .floor((short) 3)
                .capacity((short) 2)
                .notes("угловая")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        RoomDto dto = mapper.toDto(room, 1L);

        assertThat(dto.id()).isEqualTo(room.getId());
        assertThat(dto.number()).isEqualTo("305");
        assertThat(dto.floor()).isEqualTo((short) 3);
        assertThat(dto.capacity()).isEqualTo((short) 2);
        assertThat(dto.occupied()).isEqualTo(1L);
        assertThat(dto.notes()).isEqualTo("угловая");
        assertThat(dto.createdAt()).isEqualTo(room.getCreatedAt());
    }

    @Test
    @DisplayName("toDtoWithZeroOccupied ставит occupied = 0")
    void toDtoWithZeroOccupied_ok() {
        Room room = Room.builder()
                .id(UUID.randomUUID())
                .number("101")
                .floor((short) 1)
                .capacity((short) 2)
                .build();

        RoomDto dto = mapper.toDtoWithZeroOccupied(room);
        assertThat(dto.occupied()).isZero();
    }
}
