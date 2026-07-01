package com.clara.challenge.services.api;

import com.clara.challenge.entities.json.EventJson;
import com.clara.challenge.entities.json.TraceJson;

import java.util.Optional;

public interface EventService {

    Optional<TraceJson> acceptEvent(EventJson eventJson);
}
