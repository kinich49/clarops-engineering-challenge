package com.clara.challenge.services.internal;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.TraceTransition;

public interface TraceIngestionService {

    Trace findOrCreate(String traceId);

    TraceTransition upsert(Trace trace, Event event);
}
