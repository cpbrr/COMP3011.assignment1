package comp3011.assignment1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import comp3011.assignment1.dto.ShutdownResponse;
import comp3011.assignment1.dto.UptimeResponse;
import comp3011.assignment1.service.ShutdownService;
import comp3011.assignment1.service.UptimeService;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UptimeService uptimeService;
    private final ShutdownService shutdownService;

    public AdminController(UptimeService uptimeService, ShutdownService shutdownService) {
        this.uptimeService = uptimeService;
        this.shutdownService = shutdownService;
    }

    @GetMapping("/uptime")
    public UptimeResponse uptime() {
        return uptimeService.currentUptime();
    }

    @PostMapping("/shutdown")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ShutdownResponse shutdown() {
        if (!shutdownService.requestShutdown()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Graceful shutdown is already in progress.");
        }
        return new ShutdownResponse("Graceful shutdown requested.");
    }
}
