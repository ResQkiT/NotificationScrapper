package backend.academy.scrapper.metrics;

import backend.academy.scrapper.service.GitHubLinkService;
import backend.academy.scrapper.service.StackOverflowLinkService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ActiveLinksMetric {
    @Autowired
    private GitHubLinkService gitHubLinkService;
    @Autowired
    private StackOverflowLinkService stackOverflowLinkService;
    @Autowired
    private MeterRegistry meterRegistry;

    private final Map<String, AtomicLong> activeLinksCount = new ConcurrentHashMap<>();
    private final List<String> linkTypes = Arrays.asList("github", "stackoverflow");

    @PostConstruct
    public void initGauges() {
        for (String type : linkTypes) {
            activeLinksCount.put(type, new AtomicLong(0));
            Gauge.builder("app.links.active.count", activeLinksCount.get(type), AtomicLong::get)
                .description("Number of active links in DB by type")
                .tags(Tags.of("link_type", type))
                .register(meterRegistry);
        }

    }

    @Scheduled(fixedDelayString = "${metrics.database-scheduler-delay}")
    public void countActiveLink() {
        Long gitActiveLinks = gitHubLinkService.countLinks();
        Long stackOverflowLinks = stackOverflowLinkService.countLinks();
        activeLinksCount.get("github").set(gitActiveLinks);
        activeLinksCount.get("stackoverflow").set(stackOverflowLinks);
    }
}
