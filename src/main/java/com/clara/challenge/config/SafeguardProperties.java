package com.clara.challenge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "safeguard")
public record SafeguardProperties(boolean enabled, long offsetSeconds) {}
