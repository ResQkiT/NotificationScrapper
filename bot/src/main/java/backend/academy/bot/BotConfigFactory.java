package backend.academy.bot;

import backend.academy.bot.repository.UserRepository;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfigFactory {

    private final BotConfig botConfig;

    @Autowired
    public BotConfigFactory(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botConfig.telegramToken());
    }

    @Bean
    public UserRepository userRepository(){
        return new UserRepository();
    }

}
