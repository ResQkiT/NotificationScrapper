package backend.academy.bot.commands;

import backend.academy.bot.entity.Session;
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
        if (!(args instanceof String url) || url.isEmpty()) {
            sendMessage(session.chatId(), "Ошибка: укажите ссылку для удаления.");
            return;
        }

        if(!session.hasLink(url)){
            sendMessage(session.chatId(),"Такой ссылки не существует");
            return;
        }

        session.removeLink(url);

        sendMessage(session.chatId(), "Ссылка " + url + " удалена из отслеживаемых");
    }
}
