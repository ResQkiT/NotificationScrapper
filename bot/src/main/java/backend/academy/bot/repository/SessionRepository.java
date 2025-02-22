package backend.academy.bot.repository;

import backend.academy.bot.entity.Session;
import com.pengrad.telegrambot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class SessionRepository {

    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();

    public Session getSession(Long chatId){
        return sessions.get(chatId);
    }

    public Session createSession(Long chatId, User user){
        Session session = new Session(chatId, user);
        sessions.put(chatId, session);
        return session;
    }

    public boolean isSessionValid(Long sessionId){
        return sessions.containsKey(sessionId);
    }
}
