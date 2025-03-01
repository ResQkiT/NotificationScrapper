package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.exeptions.ScrapperException;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.UserService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkService linkService;

    private final UserService userService;

    @Autowired
    public LinkController(UserService userService, LinkService linkService) {
        this.userService = userService;
        this.linkService = linkService;
    }

    @GetMapping
    public ResponseEntity<ListLinksResponse> getTrackedLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.info("Получение отслеживаемых ссылок для чата: {}", chatId);

        if (chatId == null || chatId <= 0) {
            throw new ScrapperException("Некорректный ID чата ", HttpStatus.BAD_REQUEST);
        }

        if (!userService.userExists(chatId)) {
            throw new ScrapperException("Пользователь с данным ID не найден", HttpStatus.NOT_FOUND);
        }

        List<Link> userLinks = linkService.getAllLinks(chatId);
        List<LinkResponse> linkResponseList = new ArrayList<>();

        for (Link link : userLinks) {
            linkResponseList.add(new LinkResponse(link.id(), link.url(), link.tags(), link.filters()));
        }

        ListLinksResponse response = new ListLinksResponse(linkResponseList, userLinks.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        log.info("Попытка добавления ссылки для чата: {} с запросом: {}", chatId, request);

        if (chatId == null || chatId <= 0) {
            throw new ScrapperException("Некорректный ID чата", HttpStatus.BAD_REQUEST);
        }

        if (request == null || request.link() == null || request.link().isEmpty()) {
            throw new ScrapperException("Некорректные данные для добавления ссылки", HttpStatus.BAD_REQUEST);
        }

        if (!userService.userExists(chatId)) {
            throw new ScrapperException("Пользователь с данным ID не найден", HttpStatus.NOT_FOUND);
        }

        Link link = linkService.addLink(chatId, request);

        LinkResponse response = new LinkResponse(link.id(), link.url(), link.tags(), link.filters());

        log.info("Ссылка успешно добавлена: {}", response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        log.info("Попытка удаления ссылки для чата: {} с запросом: {}", chatId, request);

        if (chatId == null || chatId <= 0) {
            throw new ScrapperException("Некорректный ID чата", HttpStatus.BAD_REQUEST);
        }

        if (request == null || request.link() == null || request.link().isEmpty()) {
            throw new ScrapperException("Некорректные данные для удаления ссылки", HttpStatus.BAD_REQUEST);
        }

        if (!userService.userExists(chatId)) {
            throw new ScrapperException("Пользователь с данным ID не найден", HttpStatus.NOT_FOUND);
        }

        Link removedLink = linkService.removeLink(chatId, request.link());

        if (removedLink == null) {
            throw new ScrapperException("Сcылка не найдена", HttpStatus.NOT_FOUND);
        }

        LinkResponse response =
                new LinkResponse(removedLink.id(), removedLink.url(), removedLink.tags(), removedLink.filters());

        log.info("Ссылка успешно удалена: {}", response);
        return ResponseEntity.ok(response);
    }
}
