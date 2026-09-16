package ru.dstu.dormitory.appliances_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appliance", schema = "appliances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Appliance {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "room_id")
    private UUID roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private ApplianceType type;

    @Column(name = "brand", length = 128)
    private String brand;

    @Column(name = "model", length = 128)
    private String model;

    @Column(name = "power_watts", nullable = false)
    private int powerWatts;

    @Column(name = "photo_url", length = 512)
    private String photoUrl;

    @Column(name = "notes", length = 512)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ApplianceStatus status;

    @Column(name = "decision_by")
    private UUID decisionBy;

    @Column(name = "decision_at")
    private Instant decisionAt;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
