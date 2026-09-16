package ru.dstu.dormitory.auth_service.service;

import java.util.UUID;

public interface RefreshTokenService {

    record IssuedToken(String rawToken, UUID familyId) {
    }

    record RotatedToken(UUID userId, String newRawToken, UUID familyId) {
    }

    IssuedToken issueNew(UUID userId);

    RotatedToken rotate(String rawRefreshToken);

    void revoke(String rawRefreshToken);

    void revokeAllForUser(UUID userId);
}
