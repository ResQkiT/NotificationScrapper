package backend.academy.bot.clients;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScrapperClient implements IClient {

    private final ScrapperClientSender scrapperClientSender;

    public ScrapperClient(ScrapperClientSender scrapperClientSender) {
        this.scrapperClientSender = scrapperClientSender;
    }

    @Override
    public ResponseEntity<Void> registerChat(Long id) {
        return scrapperClientSender.registerChat(id);
    }

    @Override
    public ResponseEntity<Void> deleteChat(Long id) {
        return scrapperClientSender.deleteChat(id);
    }

    @Override
    public ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId) {
        return scrapperClientSender.getTrackedLinks(chatId);
    }

    @Override
    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        return scrapperClientSender.addLink(chatId, request);
    }

    @Override
    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        return scrapperClientSender.removeLink(chatId, request);
    }
}
