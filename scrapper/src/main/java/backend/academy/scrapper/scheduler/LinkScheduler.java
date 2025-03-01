package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.clients.TelegramBotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.processor.Processor;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UserService;
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
    private final LinkService linkService;
    private final UserService userService;

    private final List<Processor> processors;

    @Scheduled(fixedDelay = 3000)
    public void execute() {
        // Пока не оптимизируем моменты что несколько пользователей могут ждать одного вопроса

        userService.getAllUsers().forEach(user -> {
            List<Link> users_link = linkService.getAllLinks(user.id());
            for (Link link : users_link) {
                for (Processor processor : processors) {
                    if (processor.supports(link)) {

                        String result = processor.process(link);

                        if (result != null) {
                            telegramBotClient.sendUpdate(new LinkUpdate(link.id(), link.url(), result, link.chatsId()));
                        }
                    }
                }
            }
        });
    }
}
