package ru.dstu.dormitory.requests_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.dstu.dormitory.requests_service.domain.model.RequestStatusHistory;
import ru.dstu.dormitory.requests_service.web.dto.StatusHistoryDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatusHistoryMapper {

    @Mapping(target = "requestId", source = "request.id")
    StatusHistoryDto toDto(RequestStatusHistory entity);

    List<StatusHistoryDto> toDtoList(List<RequestStatusHistory> entities);
}
