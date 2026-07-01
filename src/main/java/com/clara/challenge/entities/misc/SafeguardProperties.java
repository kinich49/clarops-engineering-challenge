package com.clara.challenge.entities.misc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "safeguard")
public record SafeguardProperties(
        boolean enabled,
        long offsetSeconds
) {}