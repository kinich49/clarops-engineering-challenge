package com.clara.challenge.entities.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventJson {

    @JsonProperty
    private String eventId;

    @JsonProperty
    private String traceId;

    @JsonProperty
    private String eventName;

    @JsonProperty
    private EventResultJson result;

    @JsonProperty
    private Instant occurredAt;

    @JsonProperty
    private String nextExpectedEvent;

    @JsonProperty
    private Integer nextEventTtlSeconds;

    @JsonProperty(defaultValue = "false")
    private boolean finalEvent;

    @JsonProperty
    private Map<String, Object> metadata;
}
