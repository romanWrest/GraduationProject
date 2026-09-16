package ru.dstu.dormitory.appliances_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;
import ru.dstu.dormitory.appliances_service.web.dto.response.ApplianceDto;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApplianceMapper {

    ApplianceDto toDto(Appliance appliance);

    List<ApplianceDto> toDtoList(List<Appliance> appliances);
}
