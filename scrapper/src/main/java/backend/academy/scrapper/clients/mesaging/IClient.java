package backend.academy.scrapper.clients.mesaging;

import backend.academy.scrapper.dto.LinkUpdate;

public interface IClient {

    void send(LinkUpdate message);
}
