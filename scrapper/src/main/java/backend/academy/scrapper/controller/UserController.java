package backend.academy.scrapper.controller;

import backend.academy.scrapper.exeptions.ScrapperException;
import backend.academy.scrapper.service.UserService;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tg-chat")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable("id") Long id) {

        if (id == null || id <= 0) {
            throw new ScrapperException("Некорректный ID чата", HttpStatus.BAD_REQUEST);
        }

        userService.registerUser(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable("id") Long id) {

        if (id == null || id <= 0) {
            throw new ScrapperException("Некорректный ID чата", HttpStatus.BAD_REQUEST);
        }

        if (!userService.userExists(id)) {
            throw new ScrapperException("Пользователь с данным ID не найден", HttpStatus.NOT_FOUND);
        }

        userService.removeUser(id);
        return ResponseEntity.ok().build();
    }
}
