package backend.academy.scrapper.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "stackoverflow_links")
@PrimaryKeyJoinColumn(name = "link_id")
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class StackOverflowLink extends Link {
    @Column(name = "question_title")
    private String questionTitle;

    @Column(name = "username")
    private String author;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime createdAt;

    @Column(name = "preview_content", length = 200)
    private String previewContent;

    @Column(name = "last_answer_date")
    private OffsetDateTime lastAnswerDate;
}
