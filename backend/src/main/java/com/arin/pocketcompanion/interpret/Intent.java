package com.arin.pocketcompanion.interpret;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/** One thing the model thinks the user wants. Field names match the app's IntentDto exactly. */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Intent(
        String type,
        String title,
        String detail,
        String triggerAt,
        String rrule,
        String priority,
        String memoryKind,
        String date,
        Integer expiresInHours,
        Long amountPaise,
        String category,
        String note,
        Double confidence,
        Boolean needsConfirmation
) {
    Intent with(String triggerAt, String rrule, String priority, String memoryKind, String category,
                double confidence, boolean needsConfirmation) {
        return new Intent(type, title, detail, triggerAt, rrule, priority, memoryKind, date, expiresInHours,
                amountPaise, category, note, confidence, needsConfirmation);
    }
}
