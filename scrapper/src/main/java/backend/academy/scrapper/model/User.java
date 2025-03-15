package backend.academy.scrapper.model;

import jakarta.persistence.*;
import jakarta.servlet.http.PushBuilder;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "users")
    private List<Link> links = new ArrayList<>();

    public User(Long id, LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.id = id;
    }

    public User(Long id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }
}
