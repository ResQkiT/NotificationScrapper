package backend.academy.scrapper.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.filters.RateLimiterService;
import backend.academy.scrapper.filters.RateLimitingFilter;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LinkController.class)
@Import({RateLimitingFilter.class, RateLimiterService.class})
@TestPropertySource(properties = "messaging.requestPerSecond=1")
public class LinkControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LinkService linkService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        when(userService.userExists(anyLong())).thenReturn(true);
    }

    @Test
    public void testGetLinksRateLimiting() throws Exception {
        String ip = "192.168.1.1";
        Long chatId = 1L;
        when(linkService.getAllLinks(chatId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId).header("X-Forwarded-For", ip))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId).header("X-Forwarded-For", ip))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    public void testAddLinkRateLimiting() throws Exception {
        String ip = "192.168.1.2";
        Long chatId = 1L;
        String firstLink =
                """
            {
                "link": "https://example.com",
                "tags": ["tag1"],
                "filters": ["filter1"]
            }
            """;
        String secondLink =
                """
            {
                "link": "https://example.org",
                "tags": ["tag2"],
                "filters": ["filter2"]
            }
            """;

        Link mockLink = new Link("https://example.com");
        when(linkService.hasLink(eq(chatId), anyString())).thenReturn(false);
        when(linkService.addLink(eq(chatId), any(AddLinkRequest.class))).thenReturn(mockLink);

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstLink))
                .andExpect(status().isOk());

        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondLink))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    public void testRemoveLinkRateLimiting() throws Exception {
        String ip = "192.168.1.3";
        Long chatId = 1L;
        String firstRemove =
                """
            {
                "link": "https://example.com"
            }
            """;
        String secondRemove =
                """
            {
                "link": "https://example.org"
            }
            """;

        Link mockLink = new Link("https://example.com");
        when(linkService.removeLink(eq(chatId), eq("https://example.com"))).thenReturn(mockLink);
        when(linkService.removeLink(eq(chatId), eq("https://example.org"))).thenReturn(mockLink);

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRemove))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", chatId)
                        .header("X-Forwarded-For", ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRemove))
                .andExpect(status().isTooManyRequests());
    }
}
