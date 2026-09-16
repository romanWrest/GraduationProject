package ru.dstu.dormitory.requests_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.dstu.dormitory.requests_service.domain.model.RequestAttachment;
import ru.dstu.dormitory.requests_service.web.dto.AttachmentDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttachmentMapper {

    @Mapping(target = "requestId", source = "request.id")
    AttachmentDto toDto(RequestAttachment entity);

    List<AttachmentDto> toDtoList(List<RequestAttachment> entities);
}
