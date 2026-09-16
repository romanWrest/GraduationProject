package ru.dstu.dormitory.auth_service.domain.repo;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.dstu.dormitory.auth_service.domain.model.RoleCode;
import ru.dstu.dormitory.auth_service.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> search(RoleCode role, Boolean active, String text) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && Long.TYPE != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (role != null) {
                predicates.add(cb.equal(root.join("roles", JoinType.LEFT).get("code"), role));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }
            if (text != null && !text.isBlank()) {
                String like = "%" + text.toLowerCase() + "%";
                Predicate byEmail    = cb.like(cb.lower(root.get("email")), like);
                Predicate byFullName = cb.like(cb.lower(root.get("fullName")), like);
                predicates.add(cb.or(byEmail, byFullName));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
