package com.clara.challenge.services.internal;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.misc.EventDTO;

public interface EventIngestionService {

  EventDTO ingestEvent(final Event event);
}
