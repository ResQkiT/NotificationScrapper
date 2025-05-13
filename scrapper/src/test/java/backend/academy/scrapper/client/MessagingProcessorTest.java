package backend.academy.scrapper.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.clients.mesaging.IClient;
import backend.academy.scrapper.clients.mesaging.MessagingProcessor;
import backend.academy.scrapper.dto.LinkUpdate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MessagingProcessorTest {

    private MessagingProcessor messagingProcessor;
    private IClient client1;
    private IClient client2;
    private LinkUpdate message;

    @BeforeEach
    public void setUp() {
        client1 = mock(IClient.class);
        client2 = mock(IClient.class);
        message = new LinkUpdate(1L, "http://example.com", "Description", List.of(1L, 2L));
    }

    @Test
    public void testSend_SuccessWithFirstClient() {
        List<IClient> clients = List.of(client1);
        messagingProcessor = new MessagingProcessor(clients);
        when(client1.send(message)).thenReturn(true);

        boolean result = messagingProcessor.send(message);

        assertTrue(result);
        verify(client1, times(1)).send(message);
    }

    @Test
    public void testSend_FailureWithFirstClient_SuccessWithSecond() {
        List<IClient> clients = List.of(client1, client2);
        messagingProcessor = new MessagingProcessor(clients);
        when(client1.send(message)).thenThrow(new RuntimeException("Client 1 failed"));
        when(client2.send(message)).thenReturn(true);

        boolean result = messagingProcessor.send(message);

        assertTrue(result);
        verify(client1, times(1)).send(message);
        verify(client2, times(1)).send(message);
    }

    @Test
    public void testSend_AllClientsFail() {
        List<IClient> clients = List.of(client1, client2);
        messagingProcessor = new MessagingProcessor(clients);
        when(client1.send(message)).thenThrow(new RuntimeException("Client 1 failed"));
        when(client2.send(message)).thenThrow(new RuntimeException("Client 2 failed"));

        boolean result = messagingProcessor.send(message);

        assertFalse(result);
        verify(client1, times(1)).send(message);
        verify(client2, times(1)).send(message);
    }

    @Test
    public void testSend_NoClientsAvailable() {
        List<IClient> clients = Collections.emptyList();
        messagingProcessor = new MessagingProcessor(clients);

        boolean result = messagingProcessor.send(message);

        assertFalse(result);
    }

    @Test
    public void testSend_CyclicSwitching() {
        List<IClient> clients = List.of(client1, client2);
        messagingProcessor = new MessagingProcessor(clients);
        when(client1.send(message)).thenReturn(false);
        when(client2.send(message)).thenReturn(true);

        boolean result1 = messagingProcessor.send(message);
        boolean result2 = messagingProcessor.send(message);

        assertTrue(result1);
        assertTrue(result2);

        verify(client1, times(1)).send(message);
        verify(client2, times(2)).send(message);
    }
}
