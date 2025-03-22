package backend.academy.scrapper.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private Long id; // Telegram ID

    @Column(
            name = "created_at",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime createdAt;

    @ManyToMany(mappedBy = "users")
    private List<Link> links = new ArrayList<>();

    public User(Long id, OffsetDateTime createdAt) {
        this.createdAt = createdAt;
        this.id = id;
    }

    public User(Long id) {
        this.id = id;
        this.createdAt = OffsetDateTime.now();
    }
}
