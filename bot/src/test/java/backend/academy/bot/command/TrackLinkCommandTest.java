package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

import backend.academy.bot.commands.TrackLinkCommand;
import backend.academy.bot.entity.Session;
import backend.academy.bot.entity.States;
import backend.academy.bot.events.SendMessageEvent;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class TrackLinkCommandTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private Session session;

    @InjectMocks
    private TrackLinkCommand trackLinkCommand;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("Режим по умолчанию: при невалидном URL отправляется событие")
    void testNormalMode_whenInvalidUrl_thenSendEvent() {
        String invalidUrl = "invalid-url";
        when(session.chatId()).thenReturn(12345L);
        when(session.state()).thenReturn(States.DEFAULT);

        trackLinkCommand.execute(session, invalidUrl);

        verify(eventPublisher, times(1)).publishEvent(any(SendMessageEvent.class));
    }

    @Test
    @DisplayName("Режим по умолчанию: при валидном URL отправляется событие")
    void testNormalMode_whenValidUrl_thenSendInvalidEvent() {
        String validUrl = "https://stackoverflow.com/questions/12345";
        when(session.chatId()).thenReturn(12345L);
        when(session.state()).thenReturn(States.DEFAULT);

        trackLinkCommand.execute(session, validUrl);

        verify(eventPublisher, times(1)).publishEvent(any(SendMessageEvent.class));
    }

    @Test
    @DisplayName("Режим ожидания тегов: при получении тегов отправляется корректное событие")
    void testWaitingForTags_whenUserIsWaitingForTags_thenSendCorrectEvent() {
        List<String> tags = List.of("java", "programming");
        when(session.chatId()).thenReturn(12345L);
        when(session.state()).thenReturn(States.WAITING_FOR_TAGS);

        trackLinkCommand.execute(session, tags);

        verify(eventPublisher, times(1)).publishEvent(any(SendMessageEvent.class));
    }

    @Test
    @DisplayName("Проверка валидности URL: корректные и некорректные URL проверяются правильно")
    void testIsValidURL_whenUrlIsCorrectOrIncorrect_thenPerformOk() {
        assertThat(TrackLinkCommand.isValidURL("https://stackoverflow.com/questions/12345/valid-question"))
            .isTrue();
        assertThat(TrackLinkCommand.isValidURL("https://github.com/user/repository"))
            .isTrue();
        assertThat(TrackLinkCommand.isValidURL("https://github.com/user/repository/"))
            .isTrue();
        assertThat(TrackLinkCommand.isValidURL("https://stackoverflow.com/questions/invalid"))
            .isFalse();
        assertThat(TrackLinkCommand.isValidURL("https://example.com/somepage")).isFalse();
        assertThat(TrackLinkCommand.isValidURL("invalid-url")).isFalse();
    }
}
