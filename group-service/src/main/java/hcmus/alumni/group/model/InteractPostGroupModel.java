package hcmus.alumni.group.model;

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
@Table(name = "interact_post_group")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InteractPostGroupModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private InteractPostGroupId id;

    @MapsId("postGroupId")
    @ManyToOne
    private PostGroupModel postGroup;

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

    public InteractPostGroupModel(String postGroupId, String creatorId, Integer reactId) {
        this.id = new InteractPostGroupId(postGroupId, creatorId);
        this.postGroup = new PostGroupModel(postGroupId);
        this.creator = new UserModel(creatorId);
        this.react = new ReactModel(reactId);
    }
}
