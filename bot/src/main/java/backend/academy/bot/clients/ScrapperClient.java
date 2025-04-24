package backend.academy.bot.clients;

import backend.academy.bot.config.DomainsConfig;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class ScrapperClient extends ScrapperClientBase implements IClient {
    public ScrapperClient(RestClient.Builder restClientBuilder, DomainsConfig domainsConfig) {
        super(restClientBuilder, domainsConfig);
    }

    @Override
    public ResponseEntity<Void> registerChat(Long id) {
        return super.registerChat(id);
    }

    @Override
    public ResponseEntity<Void> deleteChat(Long id) {
        return super.deleteChat(id);
    }

    @Override
    public ResponseEntity<ListLinksResponse> getTrackedLinks(Long chatId) {
        return super.getTrackedLinks(chatId);
    }

    @Override
    public ResponseEntity<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        return super.addLink(chatId, request);
    }

    @Override
    public ResponseEntity<LinkResponse> removeLink(Long chatId, RemoveLinkRequest request) {
        return super.removeLink(chatId, request);
    }
}
