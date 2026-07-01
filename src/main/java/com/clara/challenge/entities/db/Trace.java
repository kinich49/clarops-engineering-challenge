package com.clara.challenge.entities.db;

import com.clara.challenge.entities.db.enums.TraceStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "clarops_challenge_schema", name = "traces")
@Getter
@Setter
@NoArgsConstructor
public class Trace {

  @Id
  @Column(name = "trace_id")
  private String traceId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TraceStatus status;

  @Column(name = "registration_datetime", nullable = false)
  private Instant registrationDatetime;

  @Column(name = "completed_datetime")
  private Instant completedDatetime;

  @OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
  private List<Event> events = new ArrayList<>();

  @OneToMany(mappedBy = "trace", fetch = FetchType.LAZY)
  private List<TraceTransition> transitions = new ArrayList<>();

  private transient int totalEvents;
}
