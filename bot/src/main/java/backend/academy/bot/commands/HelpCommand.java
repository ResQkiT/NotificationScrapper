package backend.academy.bot.commands;

import backend.academy.bot.entity.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelpCommand extends Command {

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Выводит меню с доступными командами";
    }

    @Override
    public void execute(Session session, Object object) {
        log.debug("Help command");

        String text = "Доступные команды:\n" + "/start - Запуск бота\n"
                + "/help - Получить помощь по командам\n"
                + "/track <ссылка на ресурс> - Привязать ссылку\n"
                + "/untrack <ссылка на ресурс> - Отвязать ссылку\n"
                + "/list - Ваши отслеживаемые ресурсы\n"
                + "Если у вас возникли проблемы, напишите в поддержку!";

        sendMessage(session.chatId(), text);
    }
}
