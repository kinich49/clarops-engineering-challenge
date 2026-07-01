package com.clara.challenge.filters;

import com.clara.challenge.entities.db.Event;
import com.clara.challenge.entities.db.TraceTransition;
import lombok.Data;

@Data
public class EventIngestionContext {

    private final Event newEvent;
    private final TraceTransition transition;

    private boolean shouldIngest = true;
    private String rejectionReason;
    private boolean invalid;

    public void reject(String reason) {
        this.shouldIngest = false;
        this.rejectionReason = reason;
    }

    public void rejectAsInvalid(String reason) {
        reject(reason);
        this.invalid = true;
    }

    public boolean shouldIngest() {
        return this.shouldIngest;
    }
}
