package backend.academy.scrapper.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private LocalDateTime lastUpdatedAt;

    @Column(name = "last_checked_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private LocalDateTime lastCheckedAt;

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
        this.lastUpdatedAt = LocalDateTime.now();
        this.lastCheckedAt = LocalDateTime.now();
    }

    public Link(Long id, String url, LocalDateTime lastUpdatedAt, LocalDateTime lastCheckedAt) {
        this.id = id;
        this.url = url;
        this.lastUpdatedAt = lastUpdatedAt;
        this.lastCheckedAt = lastCheckedAt;
    }
}
