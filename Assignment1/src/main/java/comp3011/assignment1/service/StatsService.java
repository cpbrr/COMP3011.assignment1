package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Service;

import comp3011.assignment1.dto.GlobalStatsResponse;

@Service
public class StatsService {

    // keep 2 totals together so they update at the same time - avoid confusion
    private final AtomicReference<GlobalStatsResponse> totals =
            new AtomicReference<>(new GlobalStatsResponse(0L, 0L));

    public void recordTokenUsage(long inputTokens, long outputTokens) {
        totals.updateAndGet(current -> new GlobalStatsResponse(
                current.inputTokens() + inputTokens,
                current.outputTokens() + outputTokens));
    }

    public GlobalStatsResponse currentStats() {
        return totals.get();
    }
}
