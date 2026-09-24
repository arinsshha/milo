package com.arin.pocketcompanion.interpret;

import com.arin.pocketcompanion.gemini.GeminiClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

@Service
public class InterpretService {

    private final GeminiClient gemini;
    private final IntentValidator validator;

    public InterpretService(GeminiClient gemini, IntentValidator validator) {
        this.gemini = gemini;
        this.validator = validator;
    }

    public InterpretResponse interpret(InterpretRequest request) {
        ZoneId zone = zone(request.timezone());
        OffsetDateTime now = now(request.now(), zone);

        String memories = request.memoriesOrEmpty().stream()
                .map(m -> "- " + m.title() + (m.date() == null ? "" : " (" + m.date() + ")"))
                .collect(Collectors.joining("\n"));

        InterpretResponse raw = gemini.interpret(request.message(), now.toString(), zone.getId(),
                memories.isEmpty() ? "(nothing yet)" : memories);

        String reply = raw.reply() == null || raw.reply().isBlank() ? "Got it." : raw.reply().trim();
        return new InterpretResponse(reply, validator.clean(raw.intents(), now));
    }

    private static ZoneId zone(String id) {
        try {
            return ZoneId.of(id);
        } catch (DateTimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown timezone: " + id);
        }
    }

    private static OffsetDateTime now(String iso, ZoneId zone) {
        try {
            return Instant.parse(iso).atZone(zone).toOffsetDateTime();
        } catch (DateTimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "now must be an ISO-8601 instant");
        }
    }
}
