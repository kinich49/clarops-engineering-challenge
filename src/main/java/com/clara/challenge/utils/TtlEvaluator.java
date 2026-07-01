package com.clara.challenge.utils;

import com.clara.challenge.entities.misc.SafeguardProperties;
import java.time.Instant;

public interface TtlEvaluator {

  static boolean isBreached(Instant expectedBefore, SafeguardProperties safeguard) {
    if (expectedBefore == null) {
      return false;
    }

    long offsetSeconds = safeguard.enabled() ? safeguard.offsetSeconds() : 0L;
    return Instant.now().isAfter(expectedBefore.plusSeconds(offsetSeconds));
  }
}
