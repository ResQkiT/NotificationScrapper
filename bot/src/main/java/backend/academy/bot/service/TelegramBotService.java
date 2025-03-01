package backend.academy.bot.service;

import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.CommandFactory;
import backend.academy.bot.entity.Session;
import backend.academy.bot.events.SendMessageEvent;
import backend.academy.bot.repository.SessionRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    @NotNull
    private final TelegramBot telegramBot;

    @NotNull
    private final SessionRepository sessionRepository;

    @NotNull
    private final CommandFactory commandFactory;

    @PostConstruct
    public void start() {
        log.info("Bot started");
        setBotCommands();
        telegramBot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                if (update.message() != null && update.message().text() != null) {
                    handleMessage(update);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void handleMessage(Update update) {
        Message message = update.message();
        String messageText = message.text().toLowerCase(Locale.getDefault());

        Long chatId = message.chat().id();
        User user = message.from();

        Session session;
        if (!sessionRepository.isSessionValid(chatId)) {
            session = sessionRepository.createSession(chatId, user);
        } else {
            session = sessionRepository.getSession(chatId);
        }

        switch (session.state()) {
            case DEFAULT -> {
                String[] parts = messageText.split(" ", 2);
                String rawCommand = parts[0];
                String args = (parts.length > 1) ? parts[1] : "";
                commandFactory.getCommand(rawCommand).execute(session, args);
            }
            case WAITING_FOR_TAGS -> {
                // парсим теги из строки
                String[] tags = messageText.split(",");
                List<String> list = Arrays.asList(tags);
                commandFactory.getCommand("/track").execute(session, list);
            }
            case WAITING_FOR_FILTERS -> {
                String[] filters = messageText.split(",");
                List<String> list = Arrays.asList(filters);
                commandFactory.getCommand("/track").execute(session, list);
            }
        }
    }

    public void sendMessage(Long chatId, String text) {
        telegramBot.execute(new SendMessage(chatId, text));
    }

    public void setBotCommands() {
        List<Command> customCommands = commandFactory.getCommandList();
        List<BotCommand> botCommands = new ArrayList<>();

        for (Command command : customCommands) {
            botCommands.add(new BotCommand(command.command(), command.description()));
        }

        SetMyCommands setMyCommands = new SetMyCommands(botCommands.toArray(new BotCommand[0]));

        telegramBot.execute(setMyCommands);
    }

    @EventListener
    public void handleSendMassage(SendMessageEvent event) {
        sendMessage(event.id(), event.text());
    }
}
