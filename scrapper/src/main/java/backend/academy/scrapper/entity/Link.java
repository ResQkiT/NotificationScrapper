package backend.academy.scrapper.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Link{

    @Getter
    private final Long id;

    @Getter
    private final String url;

    @Getter
    private final List<String> tags;

    @Getter
    private final List<String> filters;

    @Getter @Setter
    private OffsetDateTime lastUpdatedAt;

    @Getter @Setter
    private List<Long> chatsId = new ArrayList<>();
}
