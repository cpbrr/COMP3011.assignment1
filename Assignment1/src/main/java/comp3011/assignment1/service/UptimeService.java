package comp3011.assignment1.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.UptimeResponse;

@Service
public class UptimeService {

    private final Instant serverStart = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    public UptimeResponse currentUptime() {
        // truncated to match YAML timestamps (milisecond)
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        double seconds = Duration.between(serverStart, now).toNanos() / 1_000_000_000.0;
        return new UptimeResponse(serverStart.toString(), now.toString(), seconds);
    }
}
