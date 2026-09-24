package com.arin.pocketcompanion.gemini;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("gemini")
public record GeminiProperties(String apiKey, String model, String baseUrl, Duration timeout) {}
