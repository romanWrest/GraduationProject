package ru.dstu.dormitory.residents_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.dstu.dormitory.residents_service.domain.model.ResidencyHistory;
import ru.dstu.dormitory.residents_service.domain.model.Resident;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidencyHistoryDto;
import ru.dstu.dormitory.residents_service.web.dto.response.ResidentDto;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResidentMapper {

    @Mapping(target = "roomId", expression = "java(resident.getRoom() != null ? resident.getRoom().getId() : null)")
    @Mapping(target = "roomNumber", expression = "java(resident.getRoom() != null ? resident.getRoom().getNumber() : null)")
    ResidentDto toDto(Resident resident);

    List<ResidentDto> toDtoList(List<Resident> residents);

    @Mapping(target = "residentId", expression = "java(history.getResident() != null ? history.getResident().getId() : null)")
    @Mapping(target = "roomId", expression = "java(history.getRoom() != null ? history.getRoom().getId() : null)")
    @Mapping(target = "roomNumber", expression = "java(history.getRoom() != null ? history.getRoom().getNumber() : null)")
    ResidencyHistoryDto toHistoryDto(ResidencyHistory history);

    List<ResidencyHistoryDto> toHistoryDtoList(List<ResidencyHistory> histories);
}
