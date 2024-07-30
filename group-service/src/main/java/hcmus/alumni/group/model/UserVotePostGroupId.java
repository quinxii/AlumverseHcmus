package hcmus.alumni.group.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserVotePostGroupId {
	private VoteOptionPostGroupId voteOptionPostGroupId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    public UserVotePostGroupId(String userId, Integer voteId, String postGroupId) {
        this.voteOptionPostGroupId = new VoteOptionPostGroupId(voteId, postGroupId);
        this.userId = userId;
    }
}
