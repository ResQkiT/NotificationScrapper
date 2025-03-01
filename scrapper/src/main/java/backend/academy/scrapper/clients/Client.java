package backend.academy.scrapper.clients;

import org.springframework.web.client.RestClient;

public class Client {
    private final RestClient restClient;

    protected Client(RestClient restClient) {
        this.restClient = restClient;
    }

    public RestClient client() {
        return restClient;
    }
}
