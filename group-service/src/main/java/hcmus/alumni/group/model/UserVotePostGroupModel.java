package hcmus.alumni.group.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vote_post_group")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserVotePostGroupModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UserVotePostGroupId id;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @MapsId("voteOptionPostGroupId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "vote_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "post_group_id", referencedColumnName = "post_group_id", nullable = false)
    })
    @JsonBackReference
    private VoteOptionPostGroupModel voteOptionPostGroup;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserModel user;

    @UpdateTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete = false;

    public UserVotePostGroupModel(String userId, Integer voteId, String postGroupId) {
        VoteOptionPostGroupId voteOptionPostGroupId = new VoteOptionPostGroupId(voteId, postGroupId);
        this.id = new UserVotePostGroupId(voteOptionPostGroupId, userId);
        this.voteOptionPostGroup = new VoteOptionPostGroupModel(voteOptionPostGroupId);
        this.user = new UserModel(userId);
    }
}