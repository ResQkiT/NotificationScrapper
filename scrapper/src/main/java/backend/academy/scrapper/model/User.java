package backend.academy.scrapper.model;

import jakarta.persistence.*;
import jakarta.servlet.http.PushBuilder;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id; // Telegram ID

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    @ManyToMany(mappedBy = "users")
    private List<Link> links = new ArrayList<>();

    public User(Long id, Instant createdAt) {
        this.createdAt = createdAt;
        this.id = id;
    }

    public User(Long id) {
        this.id = id;
        this.createdAt = Instant.now();
    }
}
