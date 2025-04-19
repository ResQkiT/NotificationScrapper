package backend.academy.bot.commands;

import backend.academy.bot.clients.IClient;
import backend.academy.bot.dto.RemoveLinkRequest;
import backend.academy.bot.entity.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UntrackLinkCommand extends Command {

    private final IClient scrapperClient;

    @Autowired
    public UntrackLinkCommand(IClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "<url> - отписаться от обновления ссылки";
    }

    @Override
    public void execute(Session session, Object args) {
        if (!(args instanceof String url) || url.isEmpty()) {
            sendMessage(session.chatId(), "Ошибка: укажите ссылку для удаления.");
            return;
        }

        if (!session.hasLink(url)) {
            log.warn("Ссылка {} не найдена у пользователя {}", url, session.chatId());
            sendMessage(session.chatId(), "Такой ссылки не существует");
            return;
        }

        log.info("Удаление ссылки {} из локального хранилища", url);
        session.removeLink(url);

        log.info("Отправка запроса на удаление ссылки {} через scrapperClient", url);
        var removeLinkResponse = scrapperClient.removeLink(session.chatId(), new RemoveLinkRequest(url));

        if (removeLinkResponse.getStatusCode() == HttpStatusCode.valueOf(200)) {
            log.info("Ссылка {} успешно удалена у пользователя {}", url, session.chatId());
            sendMessage(session.chatId(), "Ссылка " + url + " удалена из отслеживаемых");
        } else {
            log.error(
                    "Ошибка при удалении ссылки {} у пользователя {}. Код ответа: {}",
                    url,
                    session.chatId(),
                    removeLinkResponse.getStatusCode());
            sendMessage(session.chatId(), "Что-то пошло не так");
        }
    }
}
