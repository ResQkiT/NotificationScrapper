package backend.academy.scrapper.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.service.GitHubLinkService;
import backend.academy.scrapper.service.StackOverflowLinkService;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ActiveLinksMetricTest {

    @Mock
    private GitHubLinkService gitHubLinkService;

    @Mock
    private StackOverflowLinkService stackOverflowLinkService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private ActiveLinksMetric activeLinksMetric;

    @BeforeEach
    public void setUp() {
        activeLinksMetric.initGauges();
    }

    @Test
    public void testActiveLinksMetricExistsAndUpdates() {
        when(gitHubLinkService.countLinks()).thenReturn(5L);
        when(stackOverflowLinkService.countLinks()).thenReturn(3L);

        activeLinksMetric.countActiveLink();
        verify(meterRegistry, times(1))
                .gauge(
                        argThat(id -> id.getName().equals("app.links.active.count")
                                && "github".equals(id.getTag("link_type"))),
                        any(AtomicLong.class),
                        any());

        verify(meterRegistry, times(1))
                .gauge(
                        argThat((Meter.Id) id -> ((Meter.Id) id).getName().equals("app.links.active.count")
                                && "stackoverflow".equals(id.getTag("link_type"))),
                        any(AtomicLong.class),
                        any());

        assertEquals(5L, activeLinksMetric.activeLinksCount.get("github").get());
        assertEquals(3L, activeLinksMetric.activeLinksCount.get("stackoverflow").get());
    }
}
