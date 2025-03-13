package backend.academy.bot.commands;

import backend.academy.bot.entity.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UndefinedCommand extends Command {

    @Override
    public String command() {
        return "undefined";
    }

    @Override
    public String description() {
        return "Неопознанный сценарий";
    }

    @Override
    public void execute(Session session, Object args) {
        log.debug("Undefined command received");
        String message = "Я не знаю такую команду. Используйте /help, чтобы увидеть доступные команды.";
        sendMessage(session.chatId(), message);
    }
}
