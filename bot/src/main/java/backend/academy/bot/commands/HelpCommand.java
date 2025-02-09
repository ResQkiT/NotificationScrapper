package backend.academy.bot.commands;

import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.session.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelpCommand extends Command {

    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public void execute(Session session, Object object) {
        log.debug("Help command");

        String text =  "Доступные команды:\n" +
            "/start - Запуск бота\n" +
            "/register - Регистрация в боте\n" +
            "/help - Получить помощь по командам\n" +
            "/track <ссылка на ресурс> - Привязать ссылку\n" +
            "/untrack <ссылка на ресурс> - Отвязать ссылку\n" +
            "/list - Ваши отслеживаемые ресурсы\n" +
            "Если у вас возникли проблемы, напишите в поддержку!";

        sendMessage(session.chatId(), text);
    }
}
