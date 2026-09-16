package ru.dstu.dormitory.notifications_service.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notification_settings", schema = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled;

    /**
     * Карта вида: { "REQUEST_CREATED": { "email": false, "inApp": true } }.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "by_type", nullable = false, columnDefinition = "jsonb")
    private Map<String, Map<String, Boolean>> byType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static NotificationSettings defaultsFor(UUID userId) {
        return NotificationSettings.builder()
                .userId(userId)
                .emailEnabled(true)
                .inAppEnabled(true)
                .byType(new HashMap<>())
                .updatedAt(Instant.now())
                .build();
    }
}
