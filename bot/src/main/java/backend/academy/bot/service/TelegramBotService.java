package backend.academy.bot.service;

import backend.academy.bot.commands.CommandFactory;
import backend.academy.bot.events.SendMessageEvent;
import backend.academy.bot.repository.SessionRepository;
import backend.academy.bot.session.Session;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    @NotNull
    private final TelegramBot telegramBot;

    @NotNull
    private final ApplicationContext context;

    @NotNull
    private final SessionRepository sessionRepository;

    @NotNull
    private final CommandFactory commandFactory;

    @PostConstruct
    public void start(){
        log.info("Bot started");
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    handleMessage(update);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleMessage(Update update) {
        Message message = update.message();
        String messageText = message.text().toLowerCase();

        String[] parts = messageText.split(" ", 2);
        String rawCommand = parts[0];
        String args = (parts.length > 1) ? parts[1] : "";

        Long chatId = update.message().chat().id();

        Session session = sessionRepository.validateSession(chatId);

        commandFactory.getCommand(rawCommand).execute(session, args);
    }

    public void sendMessage(Long chatId, String text) {
        telegramBot.execute(new SendMessage(chatId, text));
    }

    //не знаб насколько такой подход верный, мне нужно было как то порвать циклическую зависимость
    //TgService -> CommandFactory -> Command -> TgService
    @EventListener
    public void handleSendMassage(SendMessageEvent event) {
        sendMessage(event.id(), event.text());
    }
}
