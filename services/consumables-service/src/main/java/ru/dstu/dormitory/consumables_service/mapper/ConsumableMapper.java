package ru.dstu.dormitory.consumables_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableType;
import ru.dstu.dormitory.consumables_service.web.dto.response.ConsumableIssueDto;
import ru.dstu.dormitory.consumables_service.web.dto.response.ConsumableTypeDto;
import ru.dstu.dormitory.consumables_service.web.dto.response.StockItemDto;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConsumableMapper {

    ConsumableTypeDto toDto(ConsumableType type);

    List<ConsumableTypeDto> toDtoList(List<ConsumableType> types);

    @Mapping(target = "typeId", expression = "java(issue.getType() != null ? issue.getType().getId() : null)")
    @Mapping(target = "typeName", expression = "java(issue.getType() != null ? issue.getType().getName() : null)")
    ConsumableIssueDto toIssueDto(ConsumableIssue issue);

    List<ConsumableIssueDto> toIssueDtoList(List<ConsumableIssue> issues);

    default StockItemDto toStockItem(ConsumableType type) {
        if (type == null) {
            return null;
        }
        boolean low = type.getStock() <= type.getLowStockThreshold();
        return new StockItemDto(
                type.getId(),
                type.getName(),
                type.getUnit(),
                type.getStock(),
                type.getLowStockThreshold(),
                low
        );
    }

    default List<StockItemDto> toStockItemList(List<ConsumableType> types) {
        return types.stream().map(this::toStockItem).toList();
    }
}
