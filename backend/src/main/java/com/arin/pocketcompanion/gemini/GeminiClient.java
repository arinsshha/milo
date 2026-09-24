package com.arin.pocketcompanion.gemini;

import com.arin.pocketcompanion.interpret.InterpretResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/** Thin wrapper over Gemini's REST API using structured (JSON schema) output. */
@Component
public class GeminiClient {

    private final GeminiProperties props;
    private final RestClient http;
    private final JsonMapper json = JsonMapper.builder().build();
    private final String systemPrompt;
    private final Map<String, Object> responseSchema;

    public GeminiClient(GeminiProperties props) throws IOException {
        this.props = props;
        var factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(props.timeout());
        this.http = RestClient.builder().baseUrl(props.baseUrl()).requestFactory(factory).build();
        this.systemPrompt = new ClassPathResource("gemini/interpret-system.txt").getContentAsString(StandardCharsets.UTF_8);
        String schema = new ClassPathResource("gemini/interpret-schema.json").getContentAsString(StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> parsed = json.readValue(schema, Map.class);
        this.responseSchema = parsed;
    }

    public InterpretResponse interpret(String message, String now, String timezone, String memories) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new GeminiException("GEMINI_API_KEY is not set", null);
        }
        String system = systemPrompt
                .replace("{{now}}", now)
                .replace("{{timezone}}", timezone)
                .replace("{{memories}}", memories);

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", message)))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json",
                        "responseSchema", responseSchema
                )
        );

        try {
            GenerateResponse response = http.post()
                    .uri("/models/{model}:generateContent", props.model())
                    .header("x-goog-api-key", props.apiKey())
                    .body(body)
                    .retrieve()
                    .body(GenerateResponse.class);

            String text = firstText(response);
            return json.readValue(text, InterpretResponse.class);
        } catch (RestClientException | JacksonException e) {
            throw new GeminiException("Gemini call failed: " + e.getMessage(), e);
        }
    }

    private static String firstText(GenerateResponse r) {
        if (r == null || r.candidates() == null || r.candidates().isEmpty()) {
            throw new GeminiException("Gemini returned no candidates", null);
        }
        var parts = r.candidates().getFirst().content().parts();
        if (parts == null || parts.isEmpty() || parts.getFirst().text() == null) {
            throw new GeminiException("Gemini returned an empty answer", null);
        }
        return parts.getFirst().text();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GenerateResponse(List<Candidate> candidates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Candidate(Content content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Content(List<Part> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Part(String text) {}
}
