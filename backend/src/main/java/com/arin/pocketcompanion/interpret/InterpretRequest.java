package com.arin.pocketcompanion.interpret;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterpretRequest(
        @NotBlank @Size(max = 2000) String message,
        @NotBlank String now,
        @NotBlank String timezone,
        @Valid @Size(max = 50) List<MemoryContext> memories
) {
    public record MemoryContext(@NotBlank @Size(max = 200) String title, @Size(max = 40) String date) {}

    public List<MemoryContext> memoriesOrEmpty() {
        return memories == null ? List.of() : memories;
    }
}
