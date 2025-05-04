package backend.academy.scrapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "messaging.requestPerSecond=1")
public class LinkControllerRateLimitTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetLinksRateLimiting() throws Exception {
        String ip = "192.168.1.1";
        Long chatId = 1L;

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
                    "link": "invalid url, do not cite",
                    "tags": ["tag1"],
                    "filters": ["filter1"]
                }
                """;
        String secondLink =
                """
                {
                    "link": "invalid url, do not cite",
                    "tags": ["tag2"],
                    "filters": ["filter2"]
                }
                """;

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
                    "link": "invalid url, do not cite"
                }
            """;
        String secondRemove =
                """
               {
                    "link": "invalid url, do not cite"
                }
            """;

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
