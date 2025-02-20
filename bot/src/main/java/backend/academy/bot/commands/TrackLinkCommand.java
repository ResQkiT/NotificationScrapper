package backend.academy.bot.commands;

import backend.academy.bot.entity.Link;
import backend.academy.bot.entity.Session;
import backend.academy.bot.entity.States;
import backend.academy.bot.service.ScrapperClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TrackLinkCommand extends Command {

//    private final ScrapperClientService scrapperClient;
//
//    @Autowired
//    public TrackLinkCommand(ScrapperClientService scrapperClient) {
//        this.scrapperClient = scrapperClient;
//    }

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?" +
            "(([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,6}|" +
            "localhost|" +
            "\\d{1,3}(\\.\\d{1,3}){3})" +
            "(:\\d{1,5})?" +
            "(/\\S*)?$"
    );

    @Override
    public String getName() {
        return "/track";
    }

    private void normalMode(Session session, Object args){
        //TODO надо проверить не только что строка это ссылка но и то что эта ссылка подходит под шаблон ссылки на гит или стак
        if (!(args instanceof String url) || url.isEmpty() || !isValidURL(url)) {
            sendMessage(session.chatId(), "Передайте ссылку в качестве аргумента: /url <ссылка>.");
            return;
        }

        session.startLinkCreation(new Link(url), States.WAITING_FOR_TAGS);

        sendMessage(session.chatId(), "Введите теги через запятую: ");
    }

    private void waitingForTags(Session session, Object args){
        if(!(args instanceof List)){
            session.state(States.DEFAULT); //упали мы и не будем портить жизнь пользователю-сбросим его состояние
            throw new IllegalArgumentException("Passed object is not List<String>");
        }
        List<String> tagsList = (List<String>)args;

        session.setLinksTags(tagsList, States.WAITING_FOR_FILTERS);

        System.out.println(tagsList);

        sendMessage(session.chatId(), "Введите фильры через запятую: ");
    }

    private void waitingForFilters(Session session, Object args){
        if(!(args instanceof List)){
            session.state(States.DEFAULT);
            throw new IllegalArgumentException("Passed object is not List<String>");
        }

        List<String> filterList = (List<String>)args;

        session.setLinksFilters(filterList, States.DEFAULT);

        Link link = session.getBuildedLinkAndInvalidateIt(States.DEFAULT);

        System.out.println(filterList);

        //TODO: отправить запрос на сервер
        sendMessage(session.chatId(), "Новая ссылка: " + link.url() +" добавлена");
    }

    @Override
    public void execute(Session session, Object args) {
        switch (session.state()){
            case DEFAULT -> normalMode(session, args);
            case WAITING_FOR_TAGS -> waitingForTags(session, args);
            case WAITING_FOR_FILTERS -> waitingForFilters(session, args);
        }
        //
        //log.debug("Добавлена новая ссылка: {}", link);
    }

    private  static boolean isValidURL(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

}
