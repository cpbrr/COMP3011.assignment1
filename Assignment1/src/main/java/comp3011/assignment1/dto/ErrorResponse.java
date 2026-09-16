package comp3011.assignment1.dto;

public record ErrorResponse(String timestamp, int status, String error, String message, String path) {
}
