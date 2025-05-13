package backend.academy.bot.clients;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import org.springframework.http.ResponseEntity;

public interface IClient {
    ResponseEntity<Void> registerChat(Long id);

    ResponseEntity<Void> deleteChat(Long id);

    ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId);

    ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request);

    ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request);
}
