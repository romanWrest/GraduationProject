package ru.dstu.dormitory.residents_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.dstu.dormitory.residents_service.domain.repo.ResidentRepository;

import java.util.UUID;

/**
 * Помощник для выражений {@code @PreAuthorize} — проверка владения ресурсом жильца.
 */
@Component("residentAccess")
@RequiredArgsConstructor
public class ResidentAccess {

    private final ResidentRepository residentRepository;

    public boolean isOwner(UUID residentId, Object principal) {
        if (!(principal instanceof UserPrincipal p) || residentId == null) {
            return false;
        }
        return residentRepository.findById(residentId)
                .map(r -> r.getUserId().equals(p.userId()))
                .orElse(false);
    }
}
