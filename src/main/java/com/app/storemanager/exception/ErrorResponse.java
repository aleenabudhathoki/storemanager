package com.app.storemanager.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consistent JSON error payload returned by the REST exception handler.
 *
 * <p>{@code fieldErrors} contains validation messages keyed by request field;
 * it is empty for errors that are not field-specific.</p>
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
