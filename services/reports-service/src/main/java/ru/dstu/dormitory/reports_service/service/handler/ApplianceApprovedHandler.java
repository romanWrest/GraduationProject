package ru.dstu.dormitory.reports_service.service.handler;

import org.springframework.stereotype.Component;
import ru.dstu.dormitory.reports_service.domain.enums.ApplianceStatus;
import ru.dstu.dormitory.reports_service.domain.repo.ApplianceViewRepository;

@Component
public class ApplianceApprovedHandler extends ApplianceDecisionHandler {

    public static final String TYPE = "ApplianceApproved";

    public ApplianceApprovedHandler(ApplianceViewRepository repository) {
        super(repository);
    }

    @Override
    public String eventType() {
        return TYPE;
    }

    @Override
    protected ApplianceStatus targetStatus() {
        return ApplianceStatus.APPROVED;
    }
}
