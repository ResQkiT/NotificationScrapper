package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class IncomingUpdate {
    private Long id;
    private String url;
    private String description;
    private List<Long> tgChatIds;
}
