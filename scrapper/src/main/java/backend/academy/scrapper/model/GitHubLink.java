package backend.academy.scrapper.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "github_links")
@PrimaryKeyJoinColumn(name = "link_id") // Связь с основной таблицей links
public class GitHubLink extends Link {

    @Column(name = "issue_last_id", nullable = false)
    private Long lastIssueId;

    @Column(name = "issue_title", nullable = false)
    private String lastIssueTitle;

    @Column(name = "issue_creator_username", nullable = false)
    private String issueCreatorUsername;

    @Column(name = "issue_created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime issueCreatedAt;

    @Column(name = "issue_preview_description", length = 200)
    private String issuePreviewDescription;

    @Column(name = "pull_last_id", nullable = false)
    private Long lastPullRequestId;

    @Column(name = "pull_title", nullable = false)
    private String lastPullRequestTitle;

    @Column(name = "pull_creator_username", nullable = false)
    private String pullCreatorUsername;

    @Column(name = "pull_created_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime pullCreatedAt;

    @Column(name = "pull_preview_description", length = 200)
    private String pullPreviewDescription;
}
