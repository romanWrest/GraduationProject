package ru.dstu.dormitory.residents_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import ru.dstu.dormitory.residents_service.domain.enums.InventoryState;
import ru.dstu.dormitory.residents_service.domain.enums.InventoryType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_item", schema = "residents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "room")
public class InventoryItem {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 64)
    private InventoryType type;

    @Column(name = "serial_number", length = 128)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 32)
    private InventoryState state;

    @Column(name = "notes", length = 512)
    private String notes;

    @Column(name = "written_off_at")
    private Instant writtenOffAt;

    @Column(name = "written_off_reason", length = 512)
    private String writtenOffReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (state == null) {
            state = InventoryState.NEW;
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
