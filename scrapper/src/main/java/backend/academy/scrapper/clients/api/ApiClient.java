package backend.academy.scrapper.clients.api;

import org.springframework.web.client.RestClient;

public class ApiClient {
    private final RestClient restClient;

    protected ApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public RestClient client() {
        return restClient;
    }
}
