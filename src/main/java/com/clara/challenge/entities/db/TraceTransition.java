package com.clara.challenge.entities.db;

import com.clara.challenge.entities.db.enums.TraceStatus;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "clarops_challenge_schema", name = "trace_transitions")
@Getter
@Setter
@NoArgsConstructor
public class TraceTransition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "trace_id", nullable = false)
  private Trace trace;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TraceStatus status;

  @Column(name = "expected_before")
  private Instant expectedBefore;

  @Column(name = "registration_datetime", nullable = false)
  private Instant registrationDatetime;
}
