package ru.dstu.dormitory.reports_service.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реестр {@link EventHandler}: ищет обработчик по типу события.
 */
@Slf4j
@Component
public class HandlerRegistry {

    private final Map<String, EventHandler> byType;

    public HandlerRegistry(List<EventHandler> handlers) {
        this.byType = handlers.stream().collect(Collectors.toUnmodifiableMap(
                EventHandler::eventType,
                Function.identity(),
                (a, b) -> {
                    log.warn("Дублирующиеся handler-ы для типа {}: {} / {}",
                            a.eventType(), a.getClass().getSimpleName(), b.getClass().getSimpleName());
                    return a;
                }));
        log.info("Зарегистрированы handler-ы: {}", byType.keySet());
    }

    public Optional<EventHandler> find(String eventType) {
        return Optional.ofNullable(byType.get(eventType));
    }
}
