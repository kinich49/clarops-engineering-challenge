package com.clara.challenge.repositories;

import com.clara.challenge.entities.db.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {

    @Query("""
            SELECT
              COUNT(CASE WHEN e.accepted = true THEN 1 END) as validEvents,
              COUNT(CASE WHEN e.accepted = false THEN 1 END) as invalidEvents
            FROM Event e
            WHERE e.trace.traceId = :traceId
            """)
    EventCounts countByTraceId(@Param("traceId") String traceId);

    @Query("""
            SELECT e
            FROM Event e
            JOIN FETCH e.trace t
            WHERE e.trace.traceId = :traceId
            ORDER BY e.occurredAt DESC
            LIMIT 1
            """)
    Optional<Event> findLatestByTraceId(@Param("traceId") String traceId);

    interface EventCounts {
        long getValidEvents();

        long getInvalidEvents();
    }
}
