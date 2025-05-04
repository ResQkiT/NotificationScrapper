package backend.academy.scrapper.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "messaging.requestPerSecond=1")
public class UserControllerRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRateLimiting() throws Exception {
        String ip = "192.168.1.10";

        mockMvc.perform(post("/tg-chat/1").header("X-Forwarded-For", ip)).andExpect(status().isOk());

        mockMvc.perform(post("/tg-chat/2").header("X-Forwarded-For", ip)).andExpect(status().isTooManyRequests());
    }
}
