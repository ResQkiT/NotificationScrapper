package backend.academy.scrapper.processor;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;

public abstract class Processor {
    private final String host;
    private final LinkService linkService;

    protected Processor(String host, LinkService service) {
        this.host = host;
        this.linkService = service;
    }

    public abstract String process(Link link);

    public boolean supports(Link link) {
        URI uri;
        try {
            uri = new URI(link.url());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return uri.getHost().equals(this.host);
    }

    public LinkService service() {
        return linkService;
    }

    protected boolean isFirstTimeProcessing(Link link) {
        return link.lastUpdatedAt() == null;
    }

    protected boolean hasUpdates(OffsetDateTime respUpdatesTime, Link link) {
        return respUpdatesTime.isAfter(link.lastUpdatedAt());
    }

    protected Link updateLink(Link link, OffsetDateTime updatedAt) {
        link.lastUpdatedAt(updatedAt);
        return service().updateLink(link);
    }
}
