package backend.academy.bot.repository;

import backend.academy.bot.session.Session;
import backend.academy.bot.service.TelegramBotService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class SessionRepository {

    //тут сохраняются все текущие живые сессии, планирую хранить около часа, типо кеш инфа о пользователе
    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public Session getSession(Long chatId) {
        return sessions.computeIfAbsent(chatId, Session::new);
    }

    //создается новая сессия, если сессия существует все данные должны удалятся
    public Session createSession(Long chatId){

        Session session = new Session(chatId);
        sessions.put(chatId, session);
        log.info("Новая сессия создана");
        return session;
    }


    public void invalidateSession(Long chatId) {
        sessions.remove(chatId);
    }

    public boolean isSessionValid(Session session){
        return isSessionValid(session.chatId());
    }

    public boolean isSessionValid(Long sessionId){
        return sessions.containsKey(sessionId);
    }

    //Каждый вызов метода обновляет жизнь сессии
    public Session validateSession(Long chatId){
        if (isSessionValid(chatId)){
            return getSession(chatId);
        }
        Session session = createSession(chatId);
        //обновить время жизни сессии
        return session;
    }
}
