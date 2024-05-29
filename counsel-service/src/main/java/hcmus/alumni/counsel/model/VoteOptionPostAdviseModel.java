package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vote_option_post_advise")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VoteOptionPostAdviseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private VoteOptionPostAdviseId id;

    @Getter(AccessLevel.NONE)
    @MapsId("postAdviseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_advise_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private PostAdviseModel postAdvise;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "vote_count", columnDefinition = "INT DEFAULT(0)")
    private Integer voteCount = 0;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "voteOptionPostAdvise", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserVotePostAdviseModel> userVotes;

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete = false;

    @Transient
    private Boolean isVoted = false;

    public VoteOptionPostAdviseModel(VoteOptionPostAdviseId id) {
        this.id = id;
    }

    public VoteOptionPostAdviseModel(int id, PostAdviseModel postAdvise, String name) {
        this.id = new VoteOptionPostAdviseId(id, postAdvise.getId());
        this.name = name;
        this.postAdvise = postAdvise;
    }

    public VoteOptionPostAdviseModel(VoteOptionPostAdviseModel other) {
        this.id = other.id;
        this.postAdvise = other.postAdvise;
        this.name = other.name;
        this.voteCount = other.voteCount;
        this.createAt = other.createAt;
        this.isDelete = other.isDelete;
        this.isVoted = other.isVoted;
    }
}
