package backend.academy.scrapper.clients;

import backend.academy.scrapper.DomainsConfig;
import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TelegramBotClient extends Client {

    @Autowired
    public TelegramBotClient(RestClient.Builder restClientBuilder, DomainsConfig domainsConfig) {
        super(restClientBuilder.baseUrl(domainsConfig.telegramBotUrl()).build());
    }

    public ResponseEntity<Void> sendUpdate(LinkUpdate linkUpdate) {
        return client().post()
                .uri("/update")
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkUpdate)
                .retrieve()
                .toBodilessEntity();
    }
}
