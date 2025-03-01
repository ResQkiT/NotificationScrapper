package backend.academy.bot.controllers;

import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.events.SendMessageEvent;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WebController {

    @NotNull
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public WebController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/update")
    public void update(@RequestBody IncomingUpdate update) {
        log.info("New http request");

        for (Long chatId : update.tgChatIds()) {
            StringBuilder message = new StringBuilder();
            message.append("Изменение на странице: ")
                    .append(update.url())
                    .append("\n")
                    .append(" Описание: ")
                    .append(update.description());

            eventPublisher.publishEvent(new SendMessageEvent(chatId, message.toString()));
        }
    }
}
