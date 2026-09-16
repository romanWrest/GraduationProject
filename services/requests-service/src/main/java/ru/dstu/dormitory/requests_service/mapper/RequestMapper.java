package ru.dstu.dormitory.requests_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.web.dto.RequestDto;
import ru.dstu.dormitory.requests_service.web.dto.RequestSummaryDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestMapper {

    RequestDto toDto(Request entity);

    RequestSummaryDto toSummaryDto(Request entity);

    List<RequestSummaryDto> toSummaryList(List<Request> list);
}
