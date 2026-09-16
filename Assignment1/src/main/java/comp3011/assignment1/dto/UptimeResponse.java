package comp3011.assignment1.dto;

// Field names are the JSON keys, fixed by the YAML schema, which also forbids extra fields.
public record UptimeResponse(String utcServerStart, String utcNow, double serverUptimeSeconds) {
}
