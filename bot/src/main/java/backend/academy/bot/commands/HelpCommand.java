package backend.academy.bot.commands;

import backend.academy.bot.entity.Session;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelpCommand extends Command {

    @Autowired
    List<Command> commands;

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Выводит меню с доступными командами";
    }

    @Override
    public void execute(Session session, Object object) {
        log.debug("Help command");
        StringBuilder sb = new StringBuilder();
        sb.append("Доступные команды:\n");
        for (Command command : commands) {
            if (command instanceof UndefinedCommand) continue;
            sb.append(command.command() + " " + command.description() + "\n");
        }
        sb.append(this.command() + " " + this.description() + "\n");
        sb.append("Если у вас возникли проблемы - обратитесь в поддержку!\n");

        sendMessage(session.chatId(), sb.toString());
    }
}
