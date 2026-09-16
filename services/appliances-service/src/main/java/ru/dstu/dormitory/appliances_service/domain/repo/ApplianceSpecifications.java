package ru.dstu.dormitory.appliances_service.domain.repo;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.appliances_service.domain.enums.ApplianceType;
import ru.dstu.dormitory.appliances_service.domain.model.Appliance;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ApplianceSpecifications {

    private ApplianceSpecifications() {
    }

    public static Specification<Appliance> search(ApplianceStatus status,
                                                  UUID residentId,
                                                  UUID userId,
                                                  UUID roomId,
                                                  ApplianceType type,
                                                  String text) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (residentId != null) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("userId"), userId));
            }
            if (roomId != null) {
                predicates.add(cb.equal(root.get("roomId"), roomId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (text != null && !text.isBlank()) {
                String like = "%" + text.toLowerCase() + "%";
                Predicate byBrand = cb.like(cb.lower(cb.coalesce(root.get("brand"), cb.literal(""))), like);
                Predicate byModel = cb.like(cb.lower(cb.coalesce(root.get("model"), cb.literal(""))), like);
                Predicate byNotes = cb.like(cb.lower(cb.coalesce(root.get("notes"), cb.literal(""))), like);
                predicates.add(cb.or(byBrand, byModel, byNotes));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
