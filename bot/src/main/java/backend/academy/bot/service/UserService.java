package backend.academy.bot.service;

import backend.academy.bot.repository.UserRepository;
import com.pengrad.telegrambot.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {

    @NotNull
    private UserRepository userRepository;

    public boolean registerUser(User user) {
        if (user == null || userRepository.userExists(user.id())) {
            log.warn("Пользователь уже существует или null: {}", user);
            return false;
        }
        userRepository.addUser(user);
        return true;
    }
}
