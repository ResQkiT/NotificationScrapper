package backend.academy.scrapper.clients.mesaging.http;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.clients.mesaging.IClient;
import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "messaging.message-transport", havingValue = "Http")
public class TelegramBotClient implements IClient {

    private final RestClient restClient;

    @Autowired
    public TelegramBotClient(RestClient.Builder restClientBuilder, DomainsConfig domainsConfig) {
        this.restClient =
                restClientBuilder.baseUrl(domainsConfig.telegramBotUrl()).build();
    }

    public ResponseEntity<Void> sendMessage(LinkUpdate linkUpdate) {
        return restClient
                .post()
                .uri("/update")
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkUpdate)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void send(LinkUpdate update) {
        Object answer = sendMessage(update);
        // какая нибуль валидация
    }
}
