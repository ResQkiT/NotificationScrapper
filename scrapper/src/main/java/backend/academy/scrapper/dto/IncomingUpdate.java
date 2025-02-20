package backend.academy.scrapper.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class IncomingUpdate {
    private Long id;
    private String url;
    private String description;
    private List<Long> tgChatIds;
}
