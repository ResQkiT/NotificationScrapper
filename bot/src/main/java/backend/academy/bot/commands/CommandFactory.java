package backend.academy.bot.commands;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandFactory {

    private final Map<String, Command> commandMap;

    @Autowired
    public CommandFactory(List<Command> commands) {
        commandMap = commands.stream().collect(Collectors.toMap(Command::command, cmd -> cmd));
    }

    public Command getCommand(String rawCommand) {
        return commandMap.getOrDefault(rawCommand, commandMap.get("undefined"));
    }

    public List<Command> getCommandList() {
        return commandMap.values().stream()
                .filter(command -> command.command().startsWith("/"))
                .toList();
    }
}
