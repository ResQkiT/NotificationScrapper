package backend.academy.bot.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommandFactory {
    private final Map<String, Command> commandMap;
    private final UndefinedCommand undefinedCommand;

    @Autowired
    @Lazy
    public CommandFactory(List<Command> commands, UndefinedCommand undefinedCommand) {
        this.undefinedCommand = undefinedCommand;
        commandMap = commands.stream().collect(Collectors.toMap(Command::getName, cmd -> cmd));
    }

    public Command getCommand(String rawCommand) {
        return commandMap.getOrDefault(rawCommand, undefinedCommand);
    }
}

