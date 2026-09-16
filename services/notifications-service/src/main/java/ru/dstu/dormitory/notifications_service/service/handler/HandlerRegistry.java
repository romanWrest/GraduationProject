package ru.dstu.dormitory.notifications_service.service.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Реестр handler-ов событий по {@code eventType}.
 */
@Slf4j
@Component
public class HandlerRegistry {

    private final Map<String, EventHandler> byType;

    public HandlerRegistry(List<EventHandler> handlers) {
        Map<String, EventHandler> map = new HashMap<>();
        for (EventHandler h : handlers) {
            EventHandler prev = map.put(h.eventType(), h);
            if (prev != null) {
                throw new IllegalStateException(
                        "Дублирующиеся handler-ы для %s: %s и %s"
                                .formatted(h.eventType(), prev.getClass().getSimpleName(),
                                        h.getClass().getSimpleName()));
            }
        }
        this.byType = Map.copyOf(map);
        log.info("Зарегистрировано event-handler-ов: {}", byType.keySet());
    }

    public Optional<EventHandler> find(String eventType) {
        return Optional.ofNullable(byType.get(eventType));
    }
}
