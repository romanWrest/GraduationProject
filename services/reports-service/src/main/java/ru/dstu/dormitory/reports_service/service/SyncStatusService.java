package ru.dstu.dormitory.reports_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dstu.dormitory.reports_service.config.KafkaTopicsProperties;
import ru.dstu.dormitory.reports_service.domain.repo.ProcessedEventRepository;
import ru.dstu.dormitory.reports_service.web.dto.response.SyncStatusDto;
import ru.dstu.dormitory.reports_service.web.dto.response.SyncStatusDto.TopicProgress;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SyncStatusService {

    private final ProcessedEventRepository repository;
    private final KafkaTopicsProperties topics;

    @Transactional(readOnly = true)
    public SyncStatusDto status() {
        Map<String, ProcessedEventRepository.TopicStats> stats = new HashMap<>();
        for (ProcessedEventRepository.TopicStats s : repository.aggregateByTopic()) {
            stats.put(s.getTopic(), s);
        }
        Instant now = Instant.now();

        List<TopicProgress> progress = List.of(topics.getUserEvents(),
                        topics.getResidentEvents(),
                        topics.getRequestEvents(),
                        topics.getApplianceEvents(),
                        topics.getConsumableEvents()).stream()
                .map(t -> {
                    ProcessedEventRepository.TopicStats s = stats.get(t);
                    if (s == null) {
                        return TopicProgress.builder().topic(t).processed(0).build();
                    }
                    Long lag = s.getLastProcessedAt() == null
                            ? null
                            : Duration.between(s.getLastProcessedAt(), now).toSeconds();
                    return TopicProgress.builder()
                            .topic(t)
                            .processed(s.getCnt())
                            .lastProcessedAt(s.getLastProcessedAt())
                            .lagSeconds(lag)
                            .build();
                })
                .toList();

        long total = progress.stream().mapToLong(TopicProgress::processed).sum();
        return SyncStatusDto.builder()
                .totalProcessed(total)
                .topics(progress)
                .build();
    }
}
