package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class AddLinkRequest {
    private String link;
    private List<String> tags;
    private List<String> filters;
}
