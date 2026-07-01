package com.clara.challenge.controllers;

import com.clara.challenge.entities.json.TraceJson;
import com.clara.challenge.services.api.TraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/traces")
@RequiredArgsConstructor
public class TraceController {

    private final TraceService traceService;

    @GetMapping("/{traceId}/status")
    public ResponseEntity<TraceJson> getTraceStatus(@PathVariable String traceId) {
        return Optional.of(traceService.getTrace(traceId))
                .map(ResponseEntity::ok)
                .orElseThrow(RuntimeException::new);
    }
}
