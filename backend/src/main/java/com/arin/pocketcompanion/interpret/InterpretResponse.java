package com.arin.pocketcompanion.interpret;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record InterpretResponse(String reply, List<Intent> intents) {}
