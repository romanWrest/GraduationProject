package ru.dstu.dormitory.residents_service.domain.repo;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.dstu.dormitory.residents_service.domain.enums.ResidentKind;
import ru.dstu.dormitory.residents_service.domain.model.Resident;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ResidentSpecifications {

    private ResidentSpecifications() {
    }

    public static Specification<Resident> search(ResidentKind kind,
                                                 UUID roomId,
                                                 String faculty,
                                                 String text,
                                                 Boolean active) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && Long.TYPE != query.getResultType()) {
                root.fetch("room", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (kind != null) {
                predicates.add(cb.equal(root.get("kind"), kind));
            }
            if (roomId != null) {
                predicates.add(cb.equal(root.get("room").get("id"), roomId));
            }
            if (faculty != null && !faculty.isBlank()) {
                predicates.add(cb.equal(root.get("faculty"), faculty));
            }
            if (active != null) {
                if (active) {
                    predicates.add(cb.isNull(root.get("evictedAt")));
                } else {
                    predicates.add(cb.isNotNull(root.get("evictedAt")));
                }
            }
            if (text != null && !text.isBlank()) {
                String like = "%" + text.toLowerCase() + "%";
                Predicate byFaculty    = cb.like(cb.lower(cb.coalesce(root.get("faculty"),     cb.literal(""))), like);
                Predicate byStudyGroup = cb.like(cb.lower(cb.coalesce(root.get("studyGroup"),  cb.literal(""))), like);
                Predicate byDepartment = cb.like(cb.lower(cb.coalesce(root.get("department"),  cb.literal(""))), like);
                Predicate byContact    = cb.like(cb.lower(cb.coalesce(root.get("contactInfo"), cb.literal(""))), like);
                predicates.add(cb.or(byFaculty, byStudyGroup, byDepartment, byContact));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
