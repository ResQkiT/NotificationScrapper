package backend.academy.scrapper.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exeptions.ScrapperException;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ExtendWith(MockitoExtension.class)
public class LinkControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LinkService linkService;

    @Mock
    private UserService userService;

    private LinkController linkController;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        linkController = new LinkController(userService, linkService);
        mockMvc = MockMvcBuilders.standaloneSetup(linkController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
    }

    @RestControllerAdvice
    public static class TestExceptionHandler {
        @ExceptionHandler(ScrapperException.class)
        public ResponseEntity<String> handleScrapperException(ScrapperException ex) {
            return ResponseEntity.status(ex.status()).body(ex.getMessage());
        }
    }

    @Test
    @DisplayName("Получение отслеживаемых ссылок: успешный запрос возвращает список ссылок")
    public void testGetTrackedLinksSuccess() throws Exception {
        Long chatId = 123L;
        Link dummyLink = mock(Link.class);
        when(dummyLink.id()).thenReturn(1L);
        when(dummyLink.url()).thenReturn("http://example.com");
        when(dummyLink.tags()).thenReturn(Collections.emptyList());
        when(dummyLink.filters()).thenReturn(Collections.emptyList());
        when(userService.userExists(chatId)).thenReturn(true);
        when(linkService.getAllLinks(chatId)).thenReturn(List.of(dummyLink));

        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.links[0].id").value(1))
            .andExpect(jsonPath("$.links[0].url").value("http://example.com"));
    }

    @Test
    @DisplayName("Получение отслеживаемых ссылок: невалидный chatId возвращает ошибку 400")
    public void testGetTrackedLinksInvalidChatId() throws Exception {
        Long chatId = 0L;
        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение отслеживаемых ссылок: если пользователь не найден, возвращается ошибка 404")
    public void testGetTrackedLinksUserNotFound() throws Exception {
        Long chatId = 123L;
        when(userService.userExists(chatId)).thenReturn(false);
        mockMvc.perform(get("/links").header("Tg-Chat-Id", chatId)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Добавление ссылки: успешный запрос возвращает добавленную ссылку")
    public void testAddLinkSuccess() throws Exception {
        Long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("t1", "t2"), List.of("f1", "f2"));
        Link dummyLink = mock(Link.class);
        when(dummyLink.id()).thenReturn(1L);
        when(dummyLink.url()).thenReturn("http://example.com");
        when(dummyLink.tags()).thenReturn(Collections.emptyList());
        when(dummyLink.filters()).thenReturn(Collections.emptyList());
        when(userService.userExists(chatId)).thenReturn(true);
        when(linkService.addLink(eq(chatId), any(AddLinkRequest.class))).thenReturn(dummyLink);

        mockMvc.perform(post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.url").value("http://example.com"));
    }

    @Test
    @DisplayName("Добавление ссылки: невалидный chatId возвращает ошибку 400")
    public void testAddLinkInvalidChatId() throws Exception {
        Long chatId = -1L;
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("t1", "t2"), List.of("f1", "f2"));
        mockMvc.perform(post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление ссылки: невалидный запрос возвращает ошибку 400")
    public void testAddLinkInvalidRequest() throws Exception {
        Long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest("", List.of("t1", "t2"), List.of("f1", "f2"));
        mockMvc.perform(post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление ссылки: если пользователь не найден, возвращается ошибка 404")
    public void testAddLinkUserNotFound() throws Exception {
        Long chatId = 123L;
        AddLinkRequest request = new AddLinkRequest("http://example.com", List.of("t1", "t2"), List.of("f1", "f2"));
        when(userService.userExists(chatId)).thenReturn(false);
        mockMvc.perform(post("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление ссылки: успешный запрос возвращает удаленную ссылку")
    public void testRemoveLinkSuccess() throws Exception {
        Long chatId = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com");
        Link dummyLink = mock(Link.class);
        when(dummyLink.id()).thenReturn(1L);
        when(dummyLink.url()).thenReturn("http://example.com");
        when(dummyLink.tags()).thenReturn(Collections.emptyList());
        when(dummyLink.filters()).thenReturn(Collections.emptyList());
        when(userService.userExists(chatId)).thenReturn(true);
        when(linkService.removeLink(chatId, "http://example.com")).thenReturn(dummyLink);

        mockMvc.perform(delete("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.url").value("http://example.com"));
    }

    @Test
    @DisplayName("Удаление ссылки: невалидный chatId возвращает ошибку 400")
    public void testRemoveLinkInvalidChatId() throws Exception {
        Long chatId = 0L;
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com");
        mockMvc.perform(delete("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Удаление ссылки: невалидный запрос возвращает ошибку 400")
    public void testRemoveLinkInvalidRequest() throws Exception {
        Long chatId = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest("");
        mockMvc.perform(delete("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Удаление ссылки: если пользователь не найден, возвращается ошибка 404")
    public void testRemoveLinkUserNotFound() throws Exception {
        Long chatId = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com");
        when(userService.userExists(chatId)).thenReturn(false);
        mockMvc.perform(delete("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Удаление ссылки: если ссылка не найдена, возвращается ошибка 404")
    public void testRemoveLinkNotFound() throws Exception {
        Long chatId = 123L;
        RemoveLinkRequest request = new RemoveLinkRequest("http://example.com");
        when(userService.userExists(chatId)).thenReturn(true);
        when(linkService.removeLink(chatId, "http://example.com")).thenReturn(null);
        mockMvc.perform(delete("/links")
                .header("Tg-Chat-Id", chatId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
}
