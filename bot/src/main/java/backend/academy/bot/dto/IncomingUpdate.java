package backend.academy.bot.dto;

import java.util.List;

public record IncomingUpdate(Long id,
                             String url,
                             String description,
                             List<Long> tgChatIds) {}
