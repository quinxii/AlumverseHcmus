package hcmus.alumni.counsel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class VoteOptionPostAdviseId {
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "TINYINT")
    private Integer voteId;

    @Column(name = "post_advise_id", length = 36, nullable = false)
    private String postAdviseId;
}
