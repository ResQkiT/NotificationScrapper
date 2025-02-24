package backend.academy.scrapper.processor;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class Processor {
    private final String host;
    private final LinkService linkService;

    protected Processor(String host, LinkService service) {
        this.host = host;
        this.linkService = service;
    }

    public abstract String process(Link link);

    public boolean supports(Link link){
        URI uri;
        try {
            uri = new URI(link.url());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        System.out.println(uri.getHost() + " " + this.host);
        return uri.getHost().equals(this.host);
    }

    public LinkService service() {
        return linkService;
    }
}
