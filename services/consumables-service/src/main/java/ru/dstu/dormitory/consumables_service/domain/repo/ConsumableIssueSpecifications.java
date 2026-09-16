package ru.dstu.dormitory.consumables_service.domain.repo;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.dstu.dormitory.consumables_service.domain.enums.IssueStatus;
import ru.dstu.dormitory.consumables_service.domain.model.ConsumableIssue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ConsumableIssueSpecifications {

    private ConsumableIssueSpecifications() {
    }

    public static Specification<ConsumableIssue> search(UUID residentId,
                                                        UUID typeId,
                                                        IssueStatus status,
                                                        Instant from,
                                                        Instant to) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && Long.TYPE != query.getResultType()) {
                root.fetch("type", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (residentId != null) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (typeId != null) {
                predicates.add(cb.equal(root.get("type").get("id"), typeId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issuedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issuedAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
