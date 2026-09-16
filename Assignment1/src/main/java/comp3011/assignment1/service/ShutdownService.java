package comp3011.assignment1.service;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ShutdownService {

    private final ConfigurableApplicationContext applicationContext;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

    public ShutdownService(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public boolean requestShutdown() {
        // make it so only 1 request can start the shutdown, others fail and get 409 conflict
        if (!shutdownRequested.compareAndSet(false, true)) {
            return false;
        }
        // shutdown gets to run on it own thread so 202 is sent back before server fully close
        new Thread(this::stopApplication, "graceful-shutdown").start();
        return true;
    }

    private void stopApplication() {
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}
