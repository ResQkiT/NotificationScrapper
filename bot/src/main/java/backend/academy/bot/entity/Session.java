package backend.academy.bot.entity;

import com.pengrad.telegrambot.model.User;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class Session {

    @Getter
    private final Long chatId;

    @Getter
    private final User user;

    @Getter
    @Setter
    private States state = States.DEFAULT;

    @Getter
    @Setter
    private List<Link> trackedLinks = new ArrayList<>();

    private Link buildedLink;

    public boolean hasLink(String url) {
        return trackedLinks.stream().anyMatch(link -> link.url().equals(url));
    }

    public boolean removeLink(String url) {
        return trackedLinks.removeIf(link -> link.url().equals(url));
    }

    public void startLinkCreation(Link link, States nextState) {
        this.state(nextState);
        this.buildedLink = link;
    }

    public void setLinksTags(List<String> tags, States nextState) {
        if (this.buildedLink == null)
            throw new IllegalStateException("Trying to add tags with current working link is null");

        buildedLink.tags(tags);
        this.state(nextState);
    }

    public void setLinksFilters(List<String> filters, States nextState) {
        if (this.buildedLink == null)
            throw new IllegalStateException("Trying to add filters with current working link is null");

        buildedLink.filters(filters);
        this.state(nextState);
    }

    public Link getBuildedLinkAndInvalidateIt(States nextState) {
        Link link = buildedLink.clone();
        this.trackedLinks.add(link);

        buildedLink = null;

        this.state(nextState);
        return link;
    }
}
