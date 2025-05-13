package backend.academy.scrapper.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@Table(name = "stackoverflow_links")
@PrimaryKeyJoinColumn(name = "link_id") // Связь с основной таблицей links
public class StackOverflowLink extends Link {

    @Column(name = "answer_last_id")
    private Long answerLastId;

    @Column(name = "answer_last_username", length = 100)
    private String answerLastUsername;

    @Column(name = "answer_created_at")
    private OffsetDateTime answerCreatedAt;

    @Column(name = "answer_preview_description", length = 200)
    private String answerPreviewDescription;

    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "comment_last_username", length = 100)
    private String commentLastUsername;

    @Column(name = "comment_created_at")
    private OffsetDateTime commentCreatedAt;

    @Column(name = "comment_preview_description", length = 200)
    private String commentPreviewDescription;
}
