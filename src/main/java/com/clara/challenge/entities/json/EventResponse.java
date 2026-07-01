package com.clara.challenge.entities.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventResponse {

    @JsonProperty
    private String traceId;
    @JsonProperty
    private String eventId;
    @JsonProperty
    private boolean accepted;
}
