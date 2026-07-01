package com.clara.challenge.services.api;

import com.clara.challenge.entities.json.TraceJson;

public interface TraceService {

  TraceJson getTrace(String traceId);
}
