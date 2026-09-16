package ru.dstu.dormitory.residents_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import ru.dstu.dormitory.residents_service.domain.model.Room;
import ru.dstu.dormitory.residents_service.web.dto.request.CreateRoomRequest;
import ru.dstu.dormitory.residents_service.web.dto.response.RoomDetailDto;
import ru.dstu.dormitory.residents_service.web.dto.response.RoomDto;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {ResidentMapper.class, InventoryMapper.class})
public interface RoomMapper {

    @Mapping(target = "occupied", source = "occupied")
    RoomDto toDto(Room room, long occupied);

    default RoomDto toDtoWithZeroOccupied(Room room) {
        return toDto(room, 0L);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Room fromCreate(CreateRoomRequest request);

    @Named("toDetail")
    @Mapping(target = "residents", source = "residents")
    @Mapping(target = "inventory", source = "inventory")
    @Mapping(target = "occupied", source = "occupied")
    RoomDetailDto toDetail(Room room,
                           long occupied,
                           List<ru.dstu.dormitory.residents_service.web.dto.response.ResidentDto> residents,
                           List<ru.dstu.dormitory.residents_service.web.dto.response.InventoryItemDto> inventory);
}
