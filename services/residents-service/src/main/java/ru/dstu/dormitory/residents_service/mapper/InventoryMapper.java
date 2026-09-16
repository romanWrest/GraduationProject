package ru.dstu.dormitory.residents_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.dstu.dormitory.residents_service.domain.model.InventoryItem;
import ru.dstu.dormitory.residents_service.web.dto.response.InventoryItemDto;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    @Mapping(target = "roomId", expression = "java(item.getRoom() != null ? item.getRoom().getId() : null)")
    InventoryItemDto toDto(InventoryItem item);

    List<InventoryItemDto> toDtoList(List<InventoryItem> items);
}
