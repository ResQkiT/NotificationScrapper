package backend.academy.bot.commands;

import backend.academy.bot.entity.Session;
import backend.academy.bot.clients.ScrapperClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartCommand extends Command {

    private final ScrapperClient scrapperClient;

    @Autowired
    public StartCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Начать работу с ботом";
    }

    @Override
    public void execute(Session session, Object args) {
        log.info("Start command executed");

        var registerChatResponse = scrapperClient.registerChat(session.chatId());

        if(registerChatResponse.getStatusCode() == HttpStatusCode.valueOf(200)){
            String message = "Вызвана команда для старта. Вы успешно зарегестрировались в системе\n" +
                "Чтобы ознакомиться с командами введите /help";

            sendMessage(session.chatId(), message);
        }else{
            log.error("Server offline. Error code:" + registerChatResponse.getStatusCode());

            String message = "К сожалению сейчас сервер не ответчает. \n" +
                "Чтобы ознакомиться с командами введите /help";
            sendMessage(session.chatId(), message);
        }

    }
}
