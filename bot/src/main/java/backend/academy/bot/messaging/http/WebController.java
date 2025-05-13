package backend.academy.bot.messaging.http;

import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.messaging.ScrapperController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class WebController extends ScrapperController {

    @Autowired
    public WebController(ApplicationEventPublisher eventPublisher) {
        super(eventPublisher);
    }

    @PostMapping("/update")
    public void update(@RequestBody IncomingUpdate update) {
        sendNotification(update);
    }
}
