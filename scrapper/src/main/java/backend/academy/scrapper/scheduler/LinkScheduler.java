package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.clients.TelegramBotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.model.User;
import backend.academy.scrapper.processor.Processor;
import backend.academy.scrapper.service.ILinkService;
import backend.academy.scrapper.service.UserService;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkScheduler {
    private final TelegramBotClient telegramBotClient;
    private final ILinkService linkService;
    private final UserService userService;

    private final List<Processor> processors;

    @Scheduled(fixedDelay = 3000)
    public void execute() {
        linkService
                .getAllLinksWithDelay(Duration.ofSeconds(5)) // TODO перенести в конфигурацию
                .forEach(link -> {
                    System.out.println("Потрогали ссылку: " + link.toString());

                    for (Processor processor : processors) {
                        if (processor.supports(link)) {
                            String text = processor.process(link);
                            if (text == null) continue;

                            telegramBotClient.sendUpdate(new LinkUpdate(
                                    link.id(),
                                    link.url(),
                                    text,
                                    link.users().stream().map(User::id).toList()));
                        }
                    }
                });
    }
}
