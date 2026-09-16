package ru.dstu.dormitory.requests_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.requests_service.config.RequestsProperties;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.repo.RequestRepository;
import ru.dstu.dormitory.requests_service.service.Impl.RequestServiceImpl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Автозакрытие заявок в статусе DONE, у которых updated_at старше N дней.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoCloseJob {

    private final RequestRepository requestRepository;
    private final RequestServiceImpl requestService;
    private final RequestsProperties requestsProperties;

    @Scheduled(fixedDelayString = "${requests.autoclose-fixed-delay-ms:3600000}")
    @Transactional
    public void runOnSchedule() {
        run();
    }

    public int run() {
        Instant threshold = Instant.now().minus(requestsProperties.getAutocloseAfterDays(), ChronoUnit.DAYS);
        List<Request> stale = requestRepository.findStaleByStatus(RequestStatus.DONE, threshold);
        int closed = 0;
        for (Request request : stale) {
            requestService.autoClose(request);
            closed++;
        }
        if (closed > 0) {
            log.info("AutoCloseJob: закрыто заявок={}, порог={}d", closed, requestsProperties.getAutocloseAfterDays());
        }
        return closed;
    }
}
