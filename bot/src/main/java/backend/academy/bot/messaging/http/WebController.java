package backend.academy.bot.messaging.http;

import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.messaging.ScrapperController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ConditionalOnProperty(name = "messaging.message-transport", havingValue = "Http")
public class WebController extends ScrapperController {

    @Autowired
    public WebController(ApplicationEventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @PostMapping("/update")
    public void update(@RequestBody IncomingUpdate update) {
        log.info("New http request");
        sendNotification(update);
    }
}
