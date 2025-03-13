package backend.academy.scrapper.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "links")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", length = 2048, unique = true, nullable = false)
    private String url;

    @Column(name = "last_updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant lastUpdatedAt;

    @Column(name = "last_checked_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant lastCheckedAt;

    @ManyToMany
    @JoinTable(
        name = "subscriptions",
        joinColumns = @JoinColumn(name = "link_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "link_tags",
        joinColumns = @JoinColumn(name = "link_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "link_filters",
        joinColumns = @JoinColumn(name = "link_id"),
        inverseJoinColumns = @JoinColumn(name = "filter_id"))
    private List<Filter> filters = new ArrayList<>();

    public Link(String url) {
        this.url = url;
        this.lastUpdatedAt = Instant.now();
        this.lastCheckedAt = Instant.now();
    }
}
