package comp3011.assignment1.dto;

// used field names by API spec
public record UptimeResponse(String utcServerStart, String utcNow, double serverUptimeSeconds) {
}
