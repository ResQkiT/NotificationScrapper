package backend.academy.bot.command;

import backend.academy.bot.commands.TrackLinkCommand;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.entity.Link;
import backend.academy.bot.entity.Session;
import backend.academy.bot.entity.States;
import backend.academy.bot.events.SendMessageEvent;
import backend.academy.bot.clients.ScrapperClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.assertj.core.api.Assertions.assertThat;

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
    void testNormalMode_whenInvalidUrl() {
        String invalidUrl = "invalid-url";
        when(session.chatId()).thenReturn(12345L);
        when(session.state()).thenReturn(States.DEFAULT);

        trackLinkCommand.execute(session, invalidUrl);

        verify(eventPublisher, times(1)).publishEvent(any(SendMessageEvent.class));
    }

    @Test
    void testNormalMode_whenValidUrl() {
        String validUrl = "https://stackoverflow.com/questions/12345";
        when(session.chatId()).thenReturn(12345L);
        when(session.state()).thenReturn(States.DEFAULT);

        trackLinkCommand.execute(session, validUrl);

        verify(eventPublisher, times(1)).publishEvent(any(SendMessageEvent.class));
    }

    @Test
    void testWaitingForTags() {
        List<String> tags = List.of("java", "programming");
        when(session.chatId()).thenReturn(12345L);
        when(session.state()).thenReturn(States.WAITING_FOR_TAGS);

        trackLinkCommand.execute(session, tags);

        verify(eventPublisher, times(1)).publishEvent(any(SendMessageEvent.class));

    }
    @Test
    void testIsValidURL() {
        assertThat(TrackLinkCommand.isValidURL("https://stackoverflow.com/questions/12345/valid-question"))
            .isTrue();
        assertThat(TrackLinkCommand.isValidURL("https://github.com/user/repository"))
            .isTrue();
        assertThat(TrackLinkCommand.isValidURL("https://github.com/user/repository/"))
            .isTrue();
        assertThat(TrackLinkCommand.isValidURL("https://stackoverflow.com/questions/invalid"))
            .isFalse();
        assertThat(TrackLinkCommand.isValidURL("https://example.com/somepage"))
            .isFalse();
        assertThat(TrackLinkCommand.isValidURL("invalid-url"))
            .isFalse();
    }
}
