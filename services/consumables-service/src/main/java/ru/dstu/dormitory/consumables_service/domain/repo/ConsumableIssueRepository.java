package ru.dstu.dormitory.consumables_service.domain.repo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsumableIssueRepository extends JpaRepository<ConsumableIssue, UUID>, JpaSpecificationExecutor<ConsumableIssue> {

    @EntityGraph(attributePaths = "type")
    Optional<ConsumableIssue> findById(UUID id);

    @EntityGraph(attributePaths = "type")
    List<ConsumableIssue> findByResidentIdAndStatus(UUID residentId, IssueStatus status);

    @EntityGraph(attributePaths = "type")
    List<ConsumableIssue> findByUserIdAndStatus(UUID userId, IssueStatus status);
}
