package com.arin.pocketcompanion.config;

import com.arin.pocketcompanion.gemini.GeminiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(GeminiException.class)
    public ProblemDetail gemini(GeminiException e) {
        log.warn("AI unavailable: {}", e.getMessage());
        // Don't leak provider details to the app. It shows a friendly fallback line instead.
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Milo's brain is unavailable right now.");
    }
}
