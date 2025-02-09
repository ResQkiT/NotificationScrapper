package backend.academy.bot.commands;

import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.session.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UntrackLinkCommand extends Command {

    @Override
    public String getName() {
        return "/untrack";
    }

    @Override
    public void execute(Session session, Object args) {
        if (!(args instanceof String link) || link.isEmpty()) {
            sendMessage(session.chatId(), "Ошибка: укажите ссылку для удаления.");
            return;
        }

        // TODO: Логика для удаления ссылки, например:
        // boolean removed = linkRepository.removeLink(session.getUserId(), link);

        // Если ссылка успешно удалена, то сообщение будет таким:
        // telegramBotService.sendMessage(session.chatId(), "Ссылка " + link + " удалена.");

        // Временно возвращаем строку
        sendMessage(session.chatId(), "Тут будет реализована машина состояний. Команда для удаления ссылки");
    }
}
