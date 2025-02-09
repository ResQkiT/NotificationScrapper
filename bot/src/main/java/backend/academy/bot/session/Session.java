package backend.academy.bot.session;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Session {

    //машина состояний
    enum STATES{
        WAITING_FOR_LINK,
        WAITING_FOR_TAGS,
        DEFAULT
    }

    private STATES state = STATES.DEFAULT;

    @Getter
    private final Long chatId;

}
