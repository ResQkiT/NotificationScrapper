package backend.academy.scrapper.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.exeptions.ScrapperException;
import backend.academy.scrapper.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    public void setup() {
        userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
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
    public void testRegisterChat_whenValidId_thenReturnOk() throws Exception {
        Long id = 123L;
        doNothing().when(userService).registerUser(id);

        mockMvc.perform(post("/tg-chat/{id}", id)).andExpect(status().isOk());

        verify(userService).registerUser(id);
    }

    @Test
    public void testRegisterChat_whenInvalidId_thenReturnBadRequest() throws Exception {
        Long invalidId = 0L;

        mockMvc.perform(post("/tg-chat/{id}", invalidId)).andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteChat_whenValidIdAndUserExists_thenReturnOk() throws Exception {
        Long id = 123L;
        when(userService.userExists(id)).thenReturn(true);
        doNothing().when(userService).removeUser(id);

        mockMvc.perform(delete("/tg-chat/{id}", id)).andExpect(status().isOk());

        verify(userService).removeUser(id);
    }

    @Test
    public void testDeleteChat_whenInvalidId_thenReturnBadRequest() throws Exception {
        Long invalidId = -1L;

        mockMvc.perform(delete("/tg-chat/{id}", invalidId)).andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteChat_whenUserNotFound_thenReturnNotFound() throws Exception {
        Long id = 123L;
        when(userService.userExists(id)).thenReturn(false);

        mockMvc.perform(delete("/tg-chat/{id}", id)).andExpect(status().isNotFound());
    }
}
