package ru.dstu.dormitory.reports_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appliances_view", schema = "reports")
public class ApplianceView {

    @Id
    @Column(name = "appliance_id", nullable = false, updatable = false)
    private UUID applianceId;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "resident_name")
    private String residentName;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_number", length = 32)
    private String roomNumber;

    @Column(name = "type", nullable = false, length = 64)
    private String type;

    @Column(name = "brand", length = 128)
    private String brand;

    @Column(name = "model", length = 128)
    private String model;

    @Column(name = "power_watts", nullable = false)
    private Integer powerWatts;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ApplianceStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
