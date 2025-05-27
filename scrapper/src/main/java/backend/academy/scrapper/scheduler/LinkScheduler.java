package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.clients.mesaging.MessagingProcessor;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.processor.Processor;
import backend.academy.scrapper.service.LinkService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkScheduler {

    private final MessagingProcessor messagingProcessor;
    private final LinkService linkService;
    private final List<Processor> processors;

    @Value("${scheduler.delay}")
    private Duration schedulerDelay;

    @Timed(value = "app.scheduler.link.execution",
        description = "Time taken to execute link scheduler",
        percentiles = {0.5, 0.95, 0.99},
        histogram = true)
    @Scheduled(fixedDelayString = "${scheduler.fixed-delay}")
    public void execute() {
        linkService.getAllLinksWithDelay(schedulerDelay).forEach(link -> {
            for (Processor processor : processors) {
                if (processor.supports(link)) {
                    String text = processor.process(link);
                    if (text == null) continue;

                    messagingProcessor.send(new LinkUpdate(
                            link.id(),
                            link.url(),
                            text,
                            link.users().stream().map(User::id).toList()));
                }
            }
        });
    }
}
