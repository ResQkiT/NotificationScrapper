package backend.academy.scrapper.clients.mesaging.http;

import backend.academy.scrapper.clients.mesaging.IClient;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
public class TelegramBotClient implements IClient {
    private final HttpMessageSender httpMessageSender;

    @Autowired
    public TelegramBotClient(HttpMessageSender httpMessageSender) {
        this.httpMessageSender = httpMessageSender;
    }

    @Override
    public boolean send(LinkUpdate update) {
        try {
            ResponseEntity<Void> response = httpMessageSender.sendMessage(update);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error sending message with HTTP", e);
            return false;
        }
    }
}
