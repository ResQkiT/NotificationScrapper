package backend.academy.scrapper.clients.mesaging;

import backend.academy.scrapper.dto.LinkUpdate;

public interface IClient {

    boolean send(LinkUpdate message);
}
