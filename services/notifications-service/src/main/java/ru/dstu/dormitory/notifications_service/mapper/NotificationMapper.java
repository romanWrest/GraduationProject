package ru.dstu.dormitory.notifications_service.mapper;

import org.mapstruct.Mapper;
import ru.dstu.dormitory.notifications_service.domain.model.Notification;
import ru.dstu.dormitory.notifications_service.domain.model.NotificationSettings;
import ru.dstu.dormitory.notifications_service.web.dto.NotificationDto;
import ru.dstu.dormitory.notifications_service.web.dto.NotificationSettingsDto;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto toDto(Notification entity);

    NotificationSettingsDto toDto(NotificationSettings entity);
}
