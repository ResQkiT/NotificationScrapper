package backend.academy.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RemoveLinkRequest {
    private String link;
}
