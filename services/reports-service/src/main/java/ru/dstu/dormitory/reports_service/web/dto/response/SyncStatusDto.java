package ru.dstu.dormitory.reports_service.web.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record SyncStatusDto(
        long totalProcessed,
        List<TopicProgress> topics
) {
    @Builder
    public record TopicProgress(
            String topic,
            long processed,
            Instant lastProcessedAt,
            Long lagSeconds
    ) {
    }
}
