package backend.academy.scrapper.dto;

import java.util.List;

public record IncomingUpdate(Long id,
                             String url,
                             String description,
                             List<Long> tgChatIds) {}
