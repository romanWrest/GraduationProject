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
import ru.dstu.dormitory.reports_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.reports_service.domain.enums.RequestType;
import ru.dstu.dormitory.reports_service.domain.enums.TargetPool;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requests_view", schema = "reports")
public class RequestView {

    @Id
    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private RequestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private RequestStatus status;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Column(name = "assignee_name")
    private String assigneeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_pool", length = 32)
    private TargetPool targetPool;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_number", length = 32)
    private String roomNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "resolution_seconds")
    private Long resolutionSeconds;

    @Column(name = "auto_closed")
    private Boolean autoClosed;
}
