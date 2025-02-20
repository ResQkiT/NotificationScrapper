package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class ListLinksResponse {
    private List<LinkResponse> links;
    private Integer size;
}
