package backend.academy.bot.commands;

import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.session.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartCommand extends Command {

    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public void execute(Session session, Object args) {
        log.info("Start command executed");

        String message = "Вызвана команда для старта.\n" +
            "Чтобы зарегистрироваться в боте, выполните команду /register";

        sendMessage(session.chatId(), message);
    }
}
