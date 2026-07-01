package com.clara.challenge.entities.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceJson {

  @JsonProperty("traceId")
  private String traceId;

  @JsonProperty("status")
  private String status;

  @JsonProperty("lastEventName")
  private String lastEventName;

  @JsonProperty("lastEventResult")
  private String lastEventResult;

  @JsonProperty("nextExpectedEvent")
  private String nextExpectedEvent;

  @JsonProperty("nextExpectedBefore")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private Instant nextExpectedBefore;

  @JsonProperty("validEvents")
  private int validEvents;

  @JsonProperty("invalidEvents")
  private int invalidEvents;
}
