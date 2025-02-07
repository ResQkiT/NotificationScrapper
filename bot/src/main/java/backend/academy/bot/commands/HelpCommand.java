package backend.academy.bot.commands;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelpCommand extends Command<String, Object> {

    @Override
    public String execute(Object object) {
        log.debug("Help command");

        return "Доступные команды:\n" +
            "/start - Запуск бота\n" +
            "/register - Регистрация в боте\n" +
            "/help - Получить помощь по командам\n" +
            "Если у вас возникли проблемы, напишите в поддержку!";
    }
}
