package com.clara.challenge.utils;

import com.clara.challenge.entities.db.enums.EventResult;
import com.clara.challenge.entities.json.EventResultJson;

public interface EntityMapper {

    static EventResult toEventResult(EventResultJson json) {
        switch (json) {
            case SUCCESS -> {
                return EventResult.SUCCESS;
            }
            case FAILURE -> {
                return EventResult.ERROR;
            }
            default -> throw new RuntimeException();
        }
    }
}
