package backend.academy.bot.commands;

import backend.academy.bot.events.SendMessageEvent;
import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.session.Session;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
abstract public class Command {

    @NotNull
    @Autowired
    private final ApplicationEventPublisher eventPublisher;

    public void noArgsExec(Session session){
        this.execute(session, null);
    }

    public abstract String getName();

    public abstract void execute(Session session, Object args);

    protected void sendMessage(Long id, String text){
        eventPublisher.publishEvent(new SendMessageEvent(id, text));
    }
}
