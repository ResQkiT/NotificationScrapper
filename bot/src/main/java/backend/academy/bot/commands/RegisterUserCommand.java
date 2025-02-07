package backend.academy.bot.commands;

import backend.academy.bot.service.UserService;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegisterUserCommand extends Command<String, User> {

    private final UserService userService;

    public RegisterUserCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String execute(User user) {
        log.debug("Register command called");

        if (userService.registerUser(user)) {
            log.info("New user added: id={}", user.id());
            return "Пользователь " + user.firstName() + " зарегистрирован!";
        }

        return "Ошибка: пользователь уже зарегистрирован или произошла ошибка!";
    }
}

