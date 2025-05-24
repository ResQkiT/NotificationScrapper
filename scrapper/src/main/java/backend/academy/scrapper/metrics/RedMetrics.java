package backend.academy.scrapper.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;
import io.micrometer.core.instrument.Timer;

@Component
public class RedMetrics {
    private final MeterRegistry registry;

    @Autowired
    public RedMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public <T> T record(String endpoint, String method, Supplier<T> supplier) {
        Timer.Sample sample = Timer.start(registry);
        try {
            T result = supplier.get();
            sample.stop(registry.timer("http_server_requests",
                "endpoint", endpoint,
                "method", method,
                "status", "success"));
            return result;
        } catch (Exception e) {
            sample.stop(registry.timer("http_server_requests",
                "endpoint", endpoint,
                "method", method,
                "status", "error"));
            throw e;
        }
    }


}
