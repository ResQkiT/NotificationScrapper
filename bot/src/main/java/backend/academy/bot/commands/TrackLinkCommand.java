package backend.academy.bot.commands;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.entity.Link;
import backend.academy.bot.entity.Session;
import backend.academy.bot.entity.States;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackLinkCommand extends Command {

    private final ScrapperClient scrapperClient;

    private static final Pattern STACKOVERFLOW_PATTERN =
            Pattern.compile("^https?://(?:www\\.|ru\\.)?stackoverflow\\.com/questions/\\d+/.*");

    private static final Pattern GITHUB_PATTERN =
            Pattern.compile("^https?://(?:www\\.)?github\\.com/[a-zA-Z0-9_-]+/[a-zA-Z0-9_.-]+/?$");

    @Autowired
    public TrackLinkCommand(ScrapperClient scrapperClient) {
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "<url> - подписаться на обновление вопроса на Stackoverflow или репозитория GitHub ";
    }

    public static boolean isValidURL(String url) {
        return STACKOVERFLOW_PATTERN.matcher(url).matches()
                || GITHUB_PATTERN.matcher(url).matches();
    }

    private void normalMode(Session session, Object args) {
        if (!(args instanceof String url) || url.isEmpty() || !isValidURL(url)) {
            sendMessage(
                    session.chatId(),
                    "Передайте ссылку на репозиторий GitHub или вопрос StackOverflow \n "
                            + " в качестве аргумента: /track <ссылка>.");
            return;
        }

        session.startLinkCreation(new Link(url), States.WAITING_FOR_TAGS);

        sendMessage(session.chatId(), "Введите теги через запятую: ");
    }

    private void waitingForTags(Session session, Object args) {
        if (!(args instanceof List)) {
            session.state(States.DEFAULT);
            throw new IllegalArgumentException("Passed object is not List<String>");
        }

        @SuppressWarnings("unchecked")
        List<String> tagsList = (List<String>) args;

        session.setLinksTags(tagsList, States.WAITING_FOR_FILTERS);

        sendMessage(session.chatId(), "Введите фильтры через запятую: ");
    }

    private void waitingForFilters(Session session, Object args) {
        if (!(args instanceof List)) {
            session.state(States.DEFAULT);
            throw new IllegalArgumentException("Passed object is not List<String>");
        }

        List<String> filterList = (List<String>) args;

        session.setLinksFilters(filterList, States.DEFAULT);

        Link link = session.getBuildedLinkAndInvalidateIt(States.DEFAULT);

        var response =
                scrapperClient.addLink(session.chatId(), new AddLinkRequest(link.url(), link.tags(), link.filters()));

        if (response.getStatusCode() == HttpStatusCode.valueOf(200)) {
            sendMessage(session.chatId(), "Новая ссылка: " + link.url() + " добавлена");
        } else {
            sendMessage(session.chatId(), "К сожалению ссылка не была отслежена. Обратитесь в поддержку...");
        }
    }

    @Override
    public void execute(Session session, Object args) {
        switch (session.state()) {
            case DEFAULT -> normalMode(session, args);
            case WAITING_FOR_TAGS -> waitingForTags(session, args);
            case WAITING_FOR_FILTERS -> waitingForFilters(session, args);
        }
    }
}
