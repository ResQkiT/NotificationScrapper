package backend.academy.bot.commands;


import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.entity.Link;
import backend.academy.bot.entity.Session;
import backend.academy.bot.clients.ScrapperClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ListCommand extends Command{

    private final ScrapperClient scrapperClient;

    @Autowired
    public ListCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "Выводит все активные ссылки пользователя";
    }

    @Override
    public void execute(Session session, Object args) {
        StringBuilder output = new StringBuilder();
        List<Link> links = new ArrayList<>();

        var getLinksResponse = scrapperClient.getTrackedLinks(session.chatId());

        if(getLinksResponse.getStatusCode() == HttpStatusCode.valueOf(200)){
            ListLinksResponse listLinksResponse = getLinksResponse.getBody();
            for (LinkResponse linkResponse : listLinksResponse.links()) {
                links.add(new Link(linkResponse.url(), linkResponse.tags(), linkResponse.filters()));
            }
            session.trackedLinks(links);
        }else{
            //Если сервер не отвечает попробуем восстановить из котекста
            links = session.trackedLinks();
        }
        //локальная копия, может быть удалена

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
