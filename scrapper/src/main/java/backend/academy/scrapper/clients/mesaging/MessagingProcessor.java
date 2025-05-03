package backend.academy.scrapper.clients.mesaging;

import backend.academy.scrapper.dto.LinkUpdate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class MessagingProcessor {

    private final List<IClient> clients;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Autowired
    public MessagingProcessor(List<IClient> clients) {
        this.clients = clients;
    }

    public boolean send(LinkUpdate message) {
        if (clients.isEmpty()) {
            log.error("No messaging clients available");
            return false;
        }

        int size = clients.size();
        int startIndex = currentIndex.getAndUpdate(prev -> (prev + 1) % size);
        int index = startIndex;

        do {
            IClient client = clients.get(index);
            try {
                if (client.send(message)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Client at index {} failed to send message: {}", index, e.getMessage());
            }
            index = (index + 1) % size;
        } while (index != startIndex);

        log.error("All clients failed to send message");
        return false;
    }
}
