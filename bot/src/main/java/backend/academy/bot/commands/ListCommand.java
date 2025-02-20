package backend.academy.bot.commands;


import backend.academy.bot.entity.Link;
import backend.academy.bot.entity.Session;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ListCommand extends Command{


    @Override
    public String getName() {
        return "/list";
    }

    @Override
    public void execute(Session session, Object args) {
        StringBuilder output = new StringBuilder();

        List<Link> links = session.trackedLinks();

        if(links.isEmpty()){
            sendMessage(session.chatId(), "Сейчас у вас нет отслеживаемых ссылок!");
            return;
        }

        String text = "Ваши отслеживаемые ссылки:\n";
        output.append(text);

        for (int i = 1; i <= links.size(); i++) {
            Link link = links.get(i-1);

            output.append(i).append(") ").append(link.url()).append("\n");
            output.append("tags: ").append(link.tags()).append("\n");
            output.append("filters: ").append(link.filters()).append("\n");
        }

        sendMessage(session.chatId(), output.toString());
    }
}
