package com.clara.challenge.services.internal;

import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.enums.TraceStatus;

public interface TraceIngestionService {

  Trace findOrCreate(String traceId);

  Trace updateStatus(Trace trace, TraceStatus status);
}
