package backend.academy.bot;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import backend.academy.bot.commands.Command;
import backend.academy.bot.commands.CommandFactory;
import backend.academy.bot.commands.UndefinedCommand;
import backend.academy.bot.entity.Session;
import backend.academy.bot.entity.States;
import backend.academy.bot.events.SendMessageEvent;
import backend.academy.bot.repository.SessionRepository;
import backend.academy.bot.service.TelegramBotService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

class TelegramBotServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private CommandFactory commandFactory;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TelegramBotService telegramBotService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        telegramBotService = new TelegramBotService(telegramBot, sessionRepository, commandFactory);
    }

    @Test
    void shouldSetBotCommandsOnStart() {
        telegramBotService.start();

        verify(telegramBot).execute(any(SetMyCommands.class));
        verify(telegramBot).setUpdatesListener(any());
    }

    @Test
    void shouldHandleUnknownCommand() {
        Long chatId = 123L;
        Update update = createUpdateWithText(chatId, "unknown_command");
        Session mockSession = mock(Session.class);
        UndefinedCommand undefinedCommand = mock(UndefinedCommand.class);

        when(sessionRepository.isSessionValid(chatId)).thenReturn(true);
        when(sessionRepository.getSession(chatId)).thenReturn(mockSession);
        when(mockSession.state()).thenReturn(States.DEFAULT);
        when(commandFactory.getCommand("unknown_command")).thenReturn(undefinedCommand);

        telegramBotService.handleMessage(update);

        verify(undefinedCommand).execute(mockSession, "");
    }

    @Test
    void shouldHandleWaitingForTagsState() {
        Long chatId = 123L;
        Update update = createUpdateWithText(chatId, "tag1,tag2");
        Session mockSession = mock(Session.class);
        Command trackCommand = mock(Command.class);

        when(sessionRepository.isSessionValid(chatId)).thenReturn(true);
        when(sessionRepository.getSession(chatId)).thenReturn(mockSession);
        when(mockSession.state()).thenReturn(States.WAITING_FOR_TAGS);
        when(commandFactory.getCommand("/track")).thenReturn(trackCommand);

        telegramBotService.handleMessage(update);

        verify(trackCommand).execute(mockSession, List.of("tag1", "tag2"));
    }

    @Test
    void shouldSendMessageCorrectly() {
        telegramBotService.sendMessage(123L, "Test message");

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot).execute(captor.capture());

        SendMessage sendMessage = captor.getValue();
        assertEquals(123L, sendMessage.getParameters().get("chat_id"));
        assertEquals("Test message", sendMessage.getParameters().get("text"));
    }

    @Test
    void shouldHandleSendMessageEvent() {
        SendMessageEvent event = new SendMessageEvent(123L, "Test event");

        telegramBotService.handleSendMassage(event);

        verify(telegramBot).execute(any(SendMessage.class));
    }

    private Update createUpdateWithText(Long chatId, String text) {
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        User user = new User(1L);

        when(chat.id()).thenReturn(chatId);
        when(message.chat()).thenReturn(chat);
        when(message.text()).thenReturn(text);
        when(message.from()).thenReturn(user);

        Update update = mock(Update.class);
        when(update.message()).thenReturn(message);
        return update;
    }
}
