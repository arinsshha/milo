package com.arin.pocketcompanion.interpret;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntentValidatorTest {

    private final IntentValidator validator = new IntentValidator();
    private final OffsetDateTime now = OffsetDateTime.parse("2026-09-24T19:40:00+05:30");

    private Intent reminder(String at, String rrule, double confidence) {
        return new Intent("create_reminder", "Pay EMI", null, at, rrule, "important", null, null, null,
                null, null, null, confidence, null);
    }

    private Intent expense(Long paise) {
        return new Intent("log_expense", null, null, null, null, null, null, null, null,
                paise, "Food", "lunch", 0.95, null);
    }

    @Test
    void goodReminderPassesAndIsNormalised() {
        var out = validator.clean(List.of(reminder("2026-10-05T09:00:00+05:30", "freq=monthly;bymonthday=5", 0.93)), now);
        assertThat(out).hasSize(1);
        assertThat(out.getFirst().priority()).isEqualTo("IMPORTANT");
        assertThat(out.getFirst().rrule()).isEqualTo("FREQ=MONTHLY;BYMONTHDAY=5");
        assertThat(out.getFirst().needsConfirmation()).isFalse();
    }

    @Test
    void reminderInThePastNeedsConfirmation() {
        var out = validator.clean(List.of(reminder("2026-09-01T09:00:00+05:30", null, 0.95)), now);
        assertThat(out.getFirst().needsConfirmation()).isTrue();
    }

    @Test
    void unsupportedRruleIsDropped() {
        var out = validator.clean(List.of(reminder("2026-10-05T09:00:00+05:30", "FREQ=HOURLY", 0.9)), now);
        assertThat(out.getFirst().rrule()).isNull();
    }

    @Test
    void lowConfidenceNeedsConfirmation() {
        var out = validator.clean(List.of(reminder("2026-10-05T09:00:00+05:30", null, 0.4)), now);
        assertThat(out.getFirst().needsConfirmation()).isTrue();
    }

    @Test
    void absurdOrMissingAmountsAreDropped() {
        assertThat(validator.clean(List.of(expense(null), expense(-5L), expense(99_99_99_999_99L)), now)).isEmpty();
        assertThat(validator.clean(List.of(expense(25_000L)), now)).hasSize(1);
    }

    @Test
    void unknownTypesAreIgnored() {
        var hacked = new Intent("delete_everything", "x", null, null, null, null, null, null, null,
                null, null, null, 1.0, null);
        assertThat(validator.clean(List.of(hacked), now)).isEmpty();
    }

    private Intent memory(String title, String note, String kind, String triggerAt, String date, Integer hours) {
        return new Intent("save_memory", title, null, triggerAt, null, null, kind, date, hours,
                null, null, note, 0.95, null);
    }

    // Shapes below are real gemini-3.5-flash-lite output that used to be dropped or lose their dates.
    @Test
    void memoryTextInNoteBecomesTitle() {
        var out = validator.clean(List.of(memory(null, "prefers tea over coffee", "PREFERENCE", null, null, null)), now);
        assertThat(out).hasSize(1);
        assertThat(out.getFirst().title()).isEqualTo("prefers tea over coffee");
        assertThat(out.getFirst().note()).isNull();
    }

    @Test
    void memoryDateInTriggerAtMovesToDate() {
        var out = validator.clean(List.of(memory("DBMS exam", null, "DATE", "2026-10-14T09:00:00+05:30", null, null)), now);
        assertThat(out.getFirst().date()).isEqualTo("2026-10-14");
        assertThat(out.getFirst().triggerAt()).isNull();
    }

    @Test
    void temporaryExpiryInTriggerAtBecomesHours() {
        var out = validator.clean(List.of(memory(null, "parked on level 2", "TEMPORARY", "2026-09-24T21:40:00+05:30", null, null)), now);
        assertThat(out.getFirst().expiresInHours()).isEqualTo(2);
        assertThat(out.getFirst().date()).isNull();
    }

    @Test
    void factWithDateIsUpgradedAndBadDatesIgnored() {
        assertThat(validator.clean(List.of(memory("Fest", null, "FACT", null, "2026-10-20", null)), now).getFirst().memoryKind())
                .isEqualTo("DATE");
        assertThat(validator.clean(List.of(memory("Fest", null, "DATE", null, "next friday", null)), now).getFirst().date())
                .isNull();
    }

    @Test
    void memoryWithNoTextAtAllIsDropped() {
        assertThat(validator.clean(List.of(memory(null, " ", "FACT", null, null, null)), now)).isEmpty();
    }

    @Test
    void forgettingAlwaysAsks() {
        var forget = new Intent("forget_memory", "DBMS exam", null, null, null, null, null, null, null,
                null, null, null, 0.99, null);
        assertThat(validator.clean(List.of(forget), now).getFirst().needsConfirmation()).isTrue();
    }
}
