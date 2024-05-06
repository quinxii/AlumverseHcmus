package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interact_post_advise")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InteractPostAdviseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private InteractPostAdviseId id;

    @MapsId("postAdviseId")
    @ManyToOne
    private PostAdviseModel postAdvise;

    @MapsId("creator")
    @ManyToOne
    @JoinColumn(name = "creator", referencedColumnName = "id", nullable = false)
    private UserModel creator;

    @JoinColumn(name = "react_id", nullable = false)
    @ManyToOne
    ReactModel react;

    @UpdateTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete = false;

    public InteractPostAdviseModel(String postAdviseId, String creatorId, Integer reactId) {
        this.id = new InteractPostAdviseId(postAdviseId, creatorId);
        this.postAdvise = new PostAdviseModel(postAdviseId);
        this.creator = new UserModel(creatorId);
        this.react = new ReactModel(reactId);
    }
}
