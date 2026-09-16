package ru.dstu.dormitory.requests_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dstu.dormitory.requests_service.config.RequestsProperties;
import ru.dstu.dormitory.requests_service.domain.enums.RequestStatus;
import ru.dstu.dormitory.requests_service.domain.enums.RequestType;
import ru.dstu.dormitory.requests_service.domain.model.Request;
import ru.dstu.dormitory.requests_service.domain.repo.RequestRepository;
import ru.dstu.dormitory.requests_service.service.Impl.RequestServiceImpl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoCloseJobTest {

    @Mock
    RequestRepository requestRepository;
    @Mock
    RequestServiceImpl requestService;

    @Test
    void closesOnlyDoneOlderThanThreshold() {
        RequestsProperties properties = new RequestsProperties();
        properties.setAutocloseAfterDays(3);
        AutoCloseJob job = new AutoCloseJob(requestRepository, requestService, properties);

        Request r1 = Request.builder()
                .id(UUID.randomUUID()).type(RequestType.OTHER).status(RequestStatus.DONE).build();
        Request r2 = Request.builder()
                .id(UUID.randomUUID()).type(RequestType.OTHER).status(RequestStatus.DONE).build();
        when(requestRepository.findStaleByStatus(eq(RequestStatus.DONE), ArgumentCaptor.forClass(Instant.class).capture()))
                .thenReturn(List.of(r1, r2));

        int closed = job.run();

        assertEquals(2, closed);
        verify(requestService).autoClose(r1);
        verify(requestService).autoClose(r2);
    }
}
