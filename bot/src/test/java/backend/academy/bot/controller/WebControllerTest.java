package backend.academy.bot.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import backend.academy.bot.messaging.http.WebController;
import backend.academy.bot.dto.IncomingUpdate;
import backend.academy.bot.events.SendMessageEvent;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class WebControllerTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WebController webController;

    @Test
    @DisplayName("Обновление: при одном chatId отправляется одно событие с корректными данными")
    void testUpdate_whenSingleChatId_thenOk() {
        IncomingUpdate update = new IncomingUpdate(1L, "https://example.com", "описание", List.of(12345L));

        webController.update(update);
        ArgumentCaptor<SendMessageEvent> eventCaptor = ArgumentCaptor.forClass(SendMessageEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        SendMessageEvent capturedEvent = eventCaptor.getValue();
        assertEquals(12345L, capturedEvent.id());
        assertEquals("Изменение на странице: https://example.com\n Описание: \nописание", capturedEvent.text());
    }

    @Test
    @DisplayName("Обновление: при нескольких chatId отправляются соответствующие события с корректными данными")
    void testUpdate_whenMultipleChatIds_thenOk() {
        IncomingUpdate update =
                new IncomingUpdate(1L, "https://example.com", "Описание изменения", List.of(12345L, 67890L));

        webController.update(update);

        ArgumentCaptor<SendMessageEvent> eventCaptor = ArgumentCaptor.forClass(SendMessageEvent.class);
        verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<SendMessageEvent> capturedEvents = eventCaptor.getAllValues();
        assertEquals(2, capturedEvents.size());

        assertEquals(12345L, capturedEvents.get(0).id());
        assertEquals(
                "Изменение на странице: https://example.com\n Описание: \nОписание изменения",
                capturedEvents.get(0).text());

        assertEquals(67890L, capturedEvents.get(1).id());
        assertEquals(
                "Изменение на странице: https://example.com\n Описание: \nОписание изменения",
                capturedEvents.get(1).text());
    }
}
