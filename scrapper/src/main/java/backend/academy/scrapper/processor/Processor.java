package backend.academy.scrapper.processor;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.http.ResponseEntity;

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
        return link.lastCheckedAt() == null || link.lastUpdatedAt() == null;
    }

    protected boolean assertSuccess(ResponseEntity<?> response, RuntimeException onExaptionCall) {
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw onExaptionCall;
        }
        return true;
    }

    protected String cutBody(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        String preview = body.length() > 200 ? body.substring(0, body.lastIndexOf(' ', 200)) + "..." : body;
        return preview;
    }
}
