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
public class UserVotePostAdviseId {
    private VoteOptionPostAdviseId voteOptionPostAdviseId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    public UserVotePostAdviseId(String userId, Integer voteId, String postAdviseId) {
        this.voteOptionPostAdviseId = new VoteOptionPostAdviseId(voteId, postAdviseId);
        this.userId = userId;
    }
}
