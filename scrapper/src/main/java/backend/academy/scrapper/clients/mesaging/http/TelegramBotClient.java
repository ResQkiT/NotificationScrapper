package backend.academy.scrapper.clients.mesaging.http;

import backend.academy.scrapper.clients.mesaging.IClient;
import backend.academy.scrapper.dto.LinkUpdate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(name = "messaging.message-transport", havingValue = "Http")
public class TelegramBotClient implements IClient {
    private final MessageSender messageSender;

    @Autowired
    public TelegramBotClient(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public void send(LinkUpdate update) {
        ResponseEntity<Void> response = messageSender.sendMessage(update);
        if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            log.info("Service is unavailable, message not sent.");
        } else if (!response.getStatusCode().is2xxSuccessful()) {
            log.info("Failed to send message: " + response.getStatusCode());
        } else {
            log.info("Message sent successfully.");
        }
    }
}
