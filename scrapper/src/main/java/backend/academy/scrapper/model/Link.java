package backend.academy.scrapper.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "links")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", length = 2048, unique = true)
    private String url;

    @Column(name = "last_updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastUpdatedAt;

    @Column(name = "last_checked_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime lastCheckedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "subscriptions",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "link_tags",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "link_filters",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "filter_id"))
    private List<Filter> filters = new ArrayList<>();

    public Link(String url) {
        this.url = url;
        this.lastUpdatedAt = OffsetDateTime.now();
        this.lastCheckedAt = OffsetDateTime.now();
    }

    public Link(Long id, String url, OffsetDateTime lastUpdatedAt, OffsetDateTime lastCheckedAt) {
        this.id = id;
        this.url = url;
        this.lastUpdatedAt = lastUpdatedAt;
        this.lastCheckedAt = lastCheckedAt;
    }

    public Link(
            String url,
            OffsetDateTime lastUpdatedAt,
            OffsetDateTime lastCheckedAt,
            List<Tag> tags,
            List<Filter> filters) {
        this.url = url;
        this.lastUpdatedAt = lastUpdatedAt;
        this.lastCheckedAt = lastCheckedAt;
        this.tags = tags;
        this.filters = filters;
    }
}
