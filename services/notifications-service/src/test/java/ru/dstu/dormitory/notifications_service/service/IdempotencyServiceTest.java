package ru.dstu.dormitory.notifications_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.dstu.dormitory.notifications_service.domain.repo.ProcessedEventRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    ProcessedEventRepository repository;

    @InjectMocks
    IdempotencyService service;

    @Test
    void duplicateMarkReturnsFalse() {
        UUID id = UUID.randomUUID();
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("unique"));

        boolean result = service.markProcessed(id, "X", "topic");
        assertThat(result).isFalse();
    }

    @Test
    void freshMarkReturnsTrue() {
        UUID id = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean result = service.markProcessed(id, "X", "topic");
        assertThat(result).isTrue();
    }

    @Test
    void isProcessedDelegatesToRepository() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        assertThat(service.isProcessed(id)).isTrue();
    }
}
