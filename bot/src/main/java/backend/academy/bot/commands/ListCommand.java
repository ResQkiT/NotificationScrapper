package backend.academy.bot.commands;

import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.session.Session;
import org.springframework.stereotype.Component;

@Component
public class ListCommand extends Command{


    @Override
    public String getName() {
        return "/list";
    }

    @Override
    public void execute(Session session, Object args) {
        String text = "Тут выведем список ваших подписок";



        sendMessage(session.chatId(), text);
    }
}
