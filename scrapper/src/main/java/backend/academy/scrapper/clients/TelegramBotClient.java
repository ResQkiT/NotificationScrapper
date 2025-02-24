package backend.academy.scrapper.clients;

import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;


@Service
public class TelegramBotClient extends Client {

    private static final String TELEGRAM_SERVICE_URL = "http://localhost:8080";

    @Autowired
    public TelegramBotClient(RestClient.Builder restClientBuilder) {
        super(restClientBuilder
            .baseUrl(TELEGRAM_SERVICE_URL)
            .build());
    }

    public ResponseEntity<Void> sendUpdate(LinkUpdate linkUpdate) {

        System.out.println(linkUpdate);
        return client().post()
            .uri("/update")
            .contentType(MediaType.APPLICATION_JSON)
            .body(linkUpdate)
            .retrieve()
            .toBodilessEntity();
    }
}
