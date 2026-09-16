package comp3011.assignment1.exception;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import comp3011.assignment1.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

    // generic message for unexpected errors so nothing internal leaks
    private static final String UNEXPECTED_MESSAGE = "An unexpected server error occurred.";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = UNEXPECTED_MESSAGE;

        if (exception instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            message = responseStatusException.getReason() == null
                    ? status.getReasonPhrase()
                    : responseStatusException.getReason();
        } else if (exception instanceof org.springframework.web.ErrorResponse errorResponse) {
            status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            message = status.getReasonPhrase();
        }

        ErrorResponse body = new ErrorResponse(
                Instant.now().truncatedTo(ChronoUnit.MILLIS).toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}
