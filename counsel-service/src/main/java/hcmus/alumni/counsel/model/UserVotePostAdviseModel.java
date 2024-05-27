package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

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

@Entity
@Table(name = "user_vote_post_advise")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserVotePostAdviseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private UserVotePostAdviseId id;

    @Getter(AccessLevel.NONE)
    @MapsId("voteOptionPostAdviseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "vote_id", referencedColumnName = "id", nullable = false),
            @JoinColumn(name = "post_advise_id", referencedColumnName = "post_advise_id", nullable = false)
    })
    private VoteOptionPostAdviseModel voteOptionPostAdvise;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserModel user;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete = false;

    public UserVotePostAdviseModel(String userId, Integer voteId, String postAdviseId) {
        VoteOptionPostAdviseId voteOptionPostAdviseId = new VoteOptionPostAdviseId(voteId, postAdviseId);
        this.id = new UserVotePostAdviseId(voteOptionPostAdviseId, userId);
        this.voteOptionPostAdvise = new VoteOptionPostAdviseModel(voteOptionPostAdviseId);
        this.user = new UserModel(userId);
    }
}
