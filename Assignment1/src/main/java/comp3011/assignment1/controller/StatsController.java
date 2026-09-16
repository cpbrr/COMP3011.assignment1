package comp3011.assignment1.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import comp3011.assignment1.dto.GlobalStatsResponse;
import comp3011.assignment1.service.StatsService;

@RestController
@RequestMapping("/api/v1/global")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping(value = "/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public GlobalStatsResponse stats() {
        return statsService.currentStats();
    }
}
