package backend.academy.bot.commands;

import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.service.UserService;
import backend.academy.bot.session.Session;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RegisterUserCommand extends Command {

    private final UserService userService;

    public RegisterUserCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getName() {
        return "/register";
    }

    @Override
    public void execute(Session session, Object args) {

        log.debug("Registration command called");
        User user = new User(session.chatId());

        if (userService.registerUser(user)) {
            log.info("New user added: id={}", user.id());
            sendMessage(session.chatId(), "User " + user.firstName() + " registered successfully!");
        } else {
            sendMessage(session.chatId(), "Error: User already been registered!");
        }
    }
}
