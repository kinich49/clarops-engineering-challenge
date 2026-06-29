package com.clara.challenge.entities;

import com.clara.challenge.entities.enums.EventResult;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Table(schema = "clarops_challenge_schema", name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

  @Id
  @Column(name = "event_id")
  private String eventId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trace_id", nullable = false)
  private Trace trace;

  @Column(name = "event_name", nullable = false)
  private String eventName;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_result", nullable = false)
  private EventResult eventResult;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "next_expected_event")
  private String nextExpectedEvent;

  @Column(name = "next_event_ttl_seconds")
  private Integer nextEventTtlSeconds;

  @Column(name = "final_event", nullable = false)
  private boolean finalEvent = false;

  @Type(JsonType.class)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata;
}
