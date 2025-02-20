package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RestController
public class WebService {

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable("id") Long id) {

        System.out.println("Registering chat with id: " + id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable("id") Long id) {

        System.out.println("Deleting chat with id: " + id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/links")
    public ResponseEntity<ListLinksResponse> getTrackedLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        System.out.println("Getting tracked links for chat: " + chatId);

        LinkResponse link1 = new LinkResponse(
            9007199254740991L,
            "https://example.com/",
            List.of("first", "second"),
            List.of("first", "second")
        );

        LinkResponse link2 = new LinkResponse(
            9007199254740992L,  // id
            "https://example.com/",  // url
            List.of("first", "second"),  // tags
            List.of("first", "second")   // filters
        );

        ListLinksResponse response = new ListLinksResponse(
            Arrays.asList(link1, link2),
            107374
        );

        System.out.println("Response size: " + response.size());
        System.out.println("Response links: " + response.links());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId,
                                                @RequestBody AddLinkRequest request) {

        System.out.println("Adding link for chat: " + chatId + " with request: " + request);

        // Пример формирования ответа
        LinkResponse response = new LinkResponse(1l, request.link(),request.tags(), request.filters() );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId,
                                                   @RequestBody RemoveLinkRequest request) {

        System.out.println("Removing link for chat: " + chatId + " with request: " + request);

        // Пример формирования ответа
        LinkResponse response = new LinkResponse(1L, request.link(), null, null);

        return ResponseEntity.ok(response);
    }
}
