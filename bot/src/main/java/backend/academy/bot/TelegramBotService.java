package backend.academy.bot;

import backend.academy.bot.commands.HelpCommand;
import backend.academy.bot.commands.RegisterUserCommand;
import backend.academy.bot.commands.StartCommand;
import backend.academy.bot.commands.UndefinedCommand;
import backend.academy.bot.service.UserService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TelegramBotService {

    @NotNull
    private final TelegramBot telegramBot;

    @NotNull
    private final ApplicationContext context;

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
        String messageText = message.text();
        Long chatId = update.message().chat().id();
        User user = message.from();

        String response = switch (messageText.toLowerCase()) {
            case "/start" -> (new StartCommand()).noArgsExec();
            case "/register" -> (new RegisterUserCommand(context.getBean(UserService.class))).execute(user);
            case "/help" -> (new HelpCommand()).noArgsExec();
            default -> (new UndefinedCommand()).noArgsExec();
        };

        sendMessage(chatId, response);
    }

    public void sendMessage(Long chatId, String text) {
        telegramBot.execute(new SendMessage(chatId, text));
    }

}
