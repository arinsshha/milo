package com.arin.pocketcompanion.interpret;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Never trust model output blindly. Drops anything malformed, normalises the rest,
 * removes duplicates, and flags anything uncertain so the app asks the user first.
 */
@Component
public class IntentValidator {

    static final double CONFIRM_BELOW = 0.75;
    private static final long MAX_PAISE = 1_00_00_000_00L; // ₹1 crore: anything above is a parsing mistake
    private static final Set<String> TYPES =
            Set.of("create_reminder", "save_memory", "log_expense", "forget_memory", "query", "chat");
    private static final Set<String> PRIORITIES = Set.of("LOW", "NORMAL", "IMPORTANT");
    private static final Set<String> MEMORY_KINDS = Set.of("DATE", "FACT", "PREFERENCE", "TEMPORARY");
    private static final Pattern RRULE = Pattern.compile("^FREQ=(DAILY|WEEKLY|MONTHLY)(;BYMONTHDAY=([1-9]|[12]\\d|3[01]))?$");

    public List<Intent> clean(List<Intent> raw, OffsetDateTime now) {
        if (raw == null) return List.of();
        List<Intent> cleaned = raw.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.type() != null && TYPES.contains(i.type()))
                .map(i -> cleanOne(i, now))
                .filter(Objects::nonNull)
                .toList();
        return dedupe(cleaned).stream().limit(10).toList();
    }

    private Intent cleanOne(Intent i, OffsetDateTime now) {
        double confidence = clamp(i.confidence());
        boolean confirm = confidence < CONFIRM_BELOW;

        return switch (i.type()) {
            case "create_reminder" -> {
                if (isBlank(i.title())) yield null;
                OffsetDateTime at = parse(i.triggerAt());
                if (at == null || at.isBefore(now.minus(Duration.ofMinutes(1)))) confirm = true;
                String rrule = i.rrule() != null && RRULE.matcher(i.rrule().toUpperCase()).matches() ? i.rrule().toUpperCase() : null;
                String priority = upperIn(i.priority(), PRIORITIES, "NORMAL");
                yield i.with(at == null ? null : at.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), rrule, priority, null, null, confidence, confirm);
            }
            case "save_memory" -> cleanMemory(i, now, confidence, confirm);
            case "log_expense" -> {
                if (i.amountPaise() == null || i.amountPaise() <= 0 || i.amountPaise() > MAX_PAISE) yield null;
                String category = isBlank(i.category()) ? "Other" : i.category().trim();
                yield i.with(null, null, null, null, category, confidence, confirm);
            }
            // Forgetting is irreversible: the app always asks, whatever the confidence.
            case "forget_memory" -> isBlank(i.title()) ? null : i.with(null, null, null, null, null, confidence, true);
            default -> i.with(null, null, null, null, null, confidence, false); // query / chat: nothing to execute
        };
    }

    /**
     * Smaller models often put a memory's text in note/detail and its date or expiry in triggerAt.
     * Move those into the fields the app reads instead of silently dropping the memory.
     */
    private static Intent cleanMemory(Intent i, OffsetDateTime now, double confidence, boolean confirm) {
        String title = firstNonBlank(i.title(), i.note(), i.detail());
        if (title == null) return null;
        String detail = isBlank(i.detail()) || i.detail().trim().equals(title) ? null : i.detail().trim();
        String kind = upperIn(i.memoryKind(), MEMORY_KINDS, "FACT");
        OffsetDateTime at = parse(i.triggerAt());

        String date = isValidDate(i.date()) ? i.date() : null;
        if (date == null && at != null && !kind.equals("TEMPORARY")) date = at.toLocalDate().toString();

        Integer expiresInHours = i.expiresInHours() != null && i.expiresInHours() > 0 ? i.expiresInHours() : null;
        if (expiresInHours == null && kind.equals("TEMPORARY") && at != null && at.isAfter(now)) {
            long minutes = Duration.between(now, at).toMinutes();
            expiresInHours = (int) Math.max(1, (minutes + 59) / 60);
        }
        if (date != null && kind.equals("FACT")) kind = "DATE";

        return new Intent(i.type(), title.trim(), detail, null, null, null, kind, date, expiresInHours,
                null, null, null, confidence, confirm);
    }

    private static boolean isValidDate(String s) {
        if (isBlank(s)) return false;
        try {
            LocalDate.parse(s);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (!isBlank(v)) return v.trim();
        return null;
    }

    /** Models sometimes repeat an intent. Keep only the most complete version of each reminder or memory. */
    private static List<Intent> dedupe(List<Intent> intents) {
        var best = new LinkedHashMap<String, Intent>();
        int n = 0;
        for (Intent i : intents) {
            boolean dedupable = (i.type().equals("create_reminder") || i.type().equals("save_memory")) && !isBlank(i.title());
            String key = dedupable ? i.type() + "|" + i.title().trim().toLowerCase() : "unique-" + n++;
            best.merge(key, i, (a, b) -> completeness(b) > completeness(a) ? b : a);
        }
        return List.copyOf(best.values());
    }

    private static int completeness(Intent i) {
        int score = 0;
        if (i.triggerAt() != null) score += 2;
        if (i.rrule() != null) score++;
        if (i.date() != null) score++;
        if (!Boolean.TRUE.equals(i.needsConfirmation())) score++;
        return score;
    }

    private static OffsetDateTime parse(String iso) {
        if (iso == null) return null;
        try {
            return OffsetDateTime.parse(iso);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static double clamp(Double c) {
        return c == null ? 0.0 : Math.max(0.0, Math.min(1.0, c));
    }

    private static String upperIn(String value, Set<String> allowed, String fallback) {
        if (value == null) return fallback;
        String v = value.trim().toUpperCase();
        return allowed.contains(v) ? v : fallback;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}