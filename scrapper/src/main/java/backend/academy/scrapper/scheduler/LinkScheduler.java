package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.clients.TelegramBotClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.processor.Processor;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkScheduler {
    private final TelegramBotClient telegramBotClient;
    private final LinkService linkService;
    private final UserService userService;

    private final List<Processor> processors;

    @Scheduled(fixedDelay = 3000)
    public void execute(){
        //Получим все ссылки
        userService.getAllUsers().stream().forEach(user ->{
            List<Link> users_link = linkService.getAllLinks(user.id());
            //System.out.println(user.id() + " " + users_link);
            //для каждой ссылки проверим что на ней
            for (Link link : users_link){
                for (Processor processor : processors){
                    if (processor.supports(link)){
                        //Данный процессор поддерживает эту ссылку
                        System.out.println(processor);

                        String result = processor.process(link);
                        if (result != null){
                            telegramBotClient.sendUpdate(new LinkUpdate(link.id(), link.url(), result, link.chatsId()));

                        }
                    }
                }
            }

            }
        );

    }
}
