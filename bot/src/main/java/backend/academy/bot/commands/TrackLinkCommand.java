package backend.academy.bot.commands;

import backend.academy.bot.repository.LinkRepository;
import backend.academy.bot.service.TelegramBotService;
import backend.academy.bot.session.Session;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class TrackLinkCommand extends Command {

    @NotNull
    private final LinkRepository linkRepository;

    public TrackLinkCommand(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public String getName() {
        return "/track";
    }

    @Override
    public void execute(Session session, Object args) {
        if (!(args instanceof String link) || link.isEmpty()) {
            sendMessage(session.chatId(), "Передайте ссылку в качестве аргумента: /link <ссылка>.");
            return;
        }

        // TODO: Добавить валидацию ссылки
        linkRepository.addLink(session.chatId(), link);
        log.debug("Добавлена новая ссылка: {}", link);

        sendMessage(session.chatId(), "Ссылка " + link + " успешно привязана к вашему аккаунту.");

        // TODO: Реализовать машину состояний для управления процессом отслеживания
    }
}
