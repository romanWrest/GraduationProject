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
import ru.dstu.dormitory.reports_service.domain.enums.ResidentKind;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "residents_view", schema = "reports")
public class ResidentView {

    @Id
    @Column(name = "resident_id", nullable = false, updatable = false)
    private UUID residentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 32)
    private ResidentKind kind;

    @Column(name = "faculty", length = 128)
    private String faculty;

    @Column(name = "study_group", length = 64)
    private String studyGroup;

    @Column(name = "department", length = 128)
    private String department;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "room_number", length = 32)
    private String roomNumber;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDate enrolledAt;

    @Column(name = "evicted_at")
    private LocalDate evictedAt;
}
