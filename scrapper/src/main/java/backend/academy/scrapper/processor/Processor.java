package backend.academy.scrapper.processor;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.ILinkService;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;

public abstract class Processor {
    private final String host;
    private final ILinkService linkService;

    protected Processor(String host, ILinkService service) {
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

    public ILinkService service() {
        return linkService;
    }

    protected boolean isFirstTimeProcessing(Link link) {
        return link.lastCheckedAt() == null;
    }

    protected boolean hasUpdates(OffsetDateTime respUpdateTime, Link link) {
        return respUpdateTime.isAfter(OffsetDateTime.from(link.lastUpdatedAt()));
    }

    protected Link touchLink(Link link) {
        link.lastCheckedAt(OffsetDateTime.now());
        return service().updateLink(link);
    }

    protected Link updateLink(Link link, OffsetDateTime updatedAt) {
        link.lastUpdatedAt(updatedAt);
        return service().updateLink(link);
    }
}
