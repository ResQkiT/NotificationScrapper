package backend.academy.bot.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
public class Link implements Cloneable {
    @Getter
    private final String url;

    @Getter
    @Setter
    private List<String> tags;

    @Getter
    @Setter
    private List<String> filters;

    @Override
    protected Link clone() {
        Link link = new Link(this.url);
        link.tags(this.tags);
        link.filters(this.filters);
        return link;
    }
}
