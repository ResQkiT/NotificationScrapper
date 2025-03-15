package backend.academy.scrapper.processor;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
        return link.lastCheckedAt() == null;
    }

    protected boolean hasUpdates(OffsetDateTime respUpdatesTime, Link link) {
        LocalDateTime respLocalDateTime = respUpdatesTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        System.out.println("Новое время:"+ respUpdatesTime);
        System.out.println("Старое время:"+ link.lastUpdatedAt());
        return respLocalDateTime.isAfter(link.lastUpdatedAt());
    }

    protected Link updateLink(Link link, OffsetDateTime updatedAt) {
        OffsetDateTime utcUpdatedAt = updatedAt.withOffsetSameInstant(ZoneOffset.UTC);
        link.lastUpdatedAt(utcUpdatedAt.toLocalDateTime());
        return service().updateLink(link);
    }
}
