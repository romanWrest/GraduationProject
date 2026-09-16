package ru.dstu.dormitory.reports_service.service.handler;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.repo.ApplianceViewRepository;

@Component
public class ApplianceRevokedHandler extends ApplianceDecisionHandler {

    public static final String TYPE = "ApplianceRevoked";

    public ApplianceRevokedHandler(ApplianceViewRepository repository) {
        super(repository);
    }

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    protected ApplianceStatus targetStatus() {
        return ApplianceStatus.REVOKED;
    }
}
