package com.clara.challenge.services.internal;

import com.clara.challenge.entities.db.Trace;

public interface TraceIngestionService {

    Trace findOrCreate(String traceId);
}
