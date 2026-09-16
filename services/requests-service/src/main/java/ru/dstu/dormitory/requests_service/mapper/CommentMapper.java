package ru.dstu.dormitory.requests_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.dstu.dormitory.requests_service.domain.model.RequestComment;
import ru.dstu.dormitory.requests_service.web.dto.CommentDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {

    @Mapping(target = "requestId", source = "request.id")
    CommentDto toDto(RequestComment entity);

    List<CommentDto> toDtoList(List<RequestComment> entities);
}
