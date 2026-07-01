package com.clara.challenge.repositories;

import com.clara.challenge.entities.db.TraceTransition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceTransitionRepository extends JpaRepository<TraceTransition, String> {

  @Query(
      """
      SELECT tt
      FROM TraceTransition tt
      JOIN FETCH tt.trace t
      WHERE tt.trace.traceId = :traceId
      ORDER BY tt.id DESC
      LIMIT 1
      """)
  Optional<TraceTransition> findLatestByTraceId(@Param("traceId") String traceId);
}
