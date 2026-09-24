package com.arin.pocketcompanion.interpret;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class InterpretController {

    private final InterpretService service;

    public InterpretController(InterpretService service) {
        this.service = service;
    }

    @PostMapping("/interpret")
    public InterpretResponse interpret(@Valid @RequestBody InterpretRequest request) {
        return service.interpret(request);
    }
}
