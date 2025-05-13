package backend.academy.bot.messaging;

import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.events.SendMessageEvent;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;

@Controller
public abstract class ScrapperController {
    @NotNull
    protected final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ScrapperController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected String generateReport(IncomingUpdate update) {
        StringBuilder message = new StringBuilder();
        message.append("Изменение на странице: ")
                .append(update.url())
                .append("\n")
                .append(" Описание: \n")
                .append(update.description());
        return message.toString();
    }

    protected void sendNotification(IncomingUpdate update) {
        String report = generateReport(update);
        for (Long chatId : update.tgChatIds()) {
            eventPublisher.publishEvent(new SendMessageEvent(chatId, report));
        }
    }
}
