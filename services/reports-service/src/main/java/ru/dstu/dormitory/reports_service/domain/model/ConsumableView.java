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
import ru.dstu.dormitory.reports_service.domain.enums.ConsumableStatus;
import ru.dstu.dormitory.reports_service.domain.enums.ReturnCondition;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consumables_view", schema = "reports")
public class ConsumableView {

    @Id
    @Column(name = "issue_id", nullable = false, updatable = false)
    private UUID issueId;

    @Column(name = "resident_id", nullable = false)
    private UUID residentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "resident_name")
    private String residentName;

    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Column(name = "type_name", length = 128)
    private String typeName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ConsumableStatus status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_condition", length = 32)
    private ReturnCondition returnCondition;
}
