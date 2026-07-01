package com.clara.challenge.services.impl;

import com.clara.challenge.entities.db.Trace;
import com.clara.challenge.entities.db.enums.TraceStatus;
import com.clara.challenge.exceptions.TraceNotFoundException;
import com.clara.challenge.repositories.TraceRepository;
import com.clara.challenge.entities.json.TraceJson;
import com.clara.challenge.repositories.TraceTransitionRepository;
import com.clara.challenge.services.api.TraceService;

import com.clara.challenge.services.internal.TraceIngestionService;
import com.clara.challenge.utils.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TraceServiceImpl implements TraceService, TraceIngestionService {

  private final TraceRepository traceRepository;
  private final TraceTransitionRepository traceTransitionRepository;

  @Override
  public TraceJson getTrace(String traceId) {
    return traceTransitionRepository.findLatestByTraceId(traceId)
            .map(JsonMapper::toJson)
            .orElseThrow(() -> new TraceNotFoundException(traceId));
  }

  @Override
  public Trace findOrCreate(String traceId) {
    return traceRepository.findById(traceId)
            .orElseGet(() -> {
              var trace = new Trace();
              trace.setTraceId(traceId);
              trace.setStatus(TraceStatus.STARTED);
              trace.setRegistrationDatetime(Instant.now());
              return traceRepository.save(trace);
            });
  }
}
