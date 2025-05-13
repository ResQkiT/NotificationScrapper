package backend.academy.bot.client;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@TestPropertySource(properties = "spring.requestPerSecond=1")
public class WebControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRateLimiting() throws Exception {
        String ip = "192.168.1.1";

        // Первый запрос должен быть успешным
        mockMvc.perform(
                        post("/update")
                                .header("X-Forwarded-For", ip)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                {
                  "id": 1,
                  "url": "https://example.com",
                  "description": "This is a sample description",
                  "tgChatIds": [123456789, 987654321]
                }
                """))
                .andExpect(status().isOk());

        // Второй запрос должен вернуть 429 (Too Many Requests)
        mockMvc.perform(
                        post("/update")
                                .header("X-Forwarded-For", ip)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                  {
                    "id": 1,
                    "url": "https://example.com",
                    "description": "This is a sample description",
                    "tgChatIds": [123456789, 987654321]
                  }
                """))
                .andExpect(status().isTooManyRequests());
    }
}
