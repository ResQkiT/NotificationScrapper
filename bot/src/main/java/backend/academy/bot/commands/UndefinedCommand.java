package backend.academy.bot.commands;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UndefinedCommand extends Command<String, Object>{

    @Override
    public String execute(Object object) {
        log.debug("Undefined command");
        return "Я не знаю такую команду";
    }
}
