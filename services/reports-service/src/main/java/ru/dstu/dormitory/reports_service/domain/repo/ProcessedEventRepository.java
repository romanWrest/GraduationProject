package ru.dstu.dormitory.reports_service.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dstu.dormitory.reports_service.domain.model.ProcessedEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    long countByTopic(String topic);

    @Query("SELECT MAX(p.processedAt) FROM ProcessedEvent p WHERE p.topic = :topic")
    Instant findLastProcessedAt(@Param("topic") String topic);

    @Query("""
            SELECT p.topic AS topic, COUNT(p) AS cnt, MAX(p.processedAt) AS lastProcessedAt
            FROM ProcessedEvent p
            GROUP BY p.topic
            """)
    List<TopicStats> aggregateByTopic();

    interface TopicStats {
        String getTopic();
        long getCnt();
        Instant getLastProcessedAt();
    }
}
