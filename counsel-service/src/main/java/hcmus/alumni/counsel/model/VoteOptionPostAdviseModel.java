package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @CreationTimestamp
    @Column(name = "create_at", nullable = false)
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete = false;

    public VoteOptionPostAdviseModel(VoteOptionPostAdviseId id) {
        this.id = id;
    }

    public VoteOptionPostAdviseModel(int id, PostAdviseModel postAdvise, String name) {
        this.id = new VoteOptionPostAdviseId(id, postAdvise.getId());
        this.name = name;
        this.postAdvise = postAdvise;
    }
}
