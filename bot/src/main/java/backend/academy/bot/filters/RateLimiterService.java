package backend.academy.bot.filters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
    private final Cache<String, Bucket> cache;

    @Value("${spring.requestPerSecond}")
    private Integer requestsPerSecond;

    public RateLimiterService() {
        this.cache = Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.HOURS).build();
    }

    public Bucket resolveBucket(String ip) {
        return cache.get(ip, k -> newBucket());
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        requestsPerSecond, Refill.intervally(requestsPerSecond, Duration.ofSeconds(1))))
                .build();
    }
}
