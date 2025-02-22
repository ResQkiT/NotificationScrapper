package backend.academy.bot.commands;

import backend.academy.bot.events.SendMessageEvent;
import backend.academy.bot.entity.Session;
import com.pengrad.telegrambot.model.BotCommand;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
abstract public class Command{

    @NotNull
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public abstract String command();

    public abstract String description();

    public abstract void execute(Session session, Object args);

    protected void sendMessage(Long id, String text){
        eventPublisher.publishEvent(new SendMessageEvent(id, text));
    }
}
