package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hcmus.alumni.group.model.StatusPostModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "`group`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "creator", nullable = false)
    private UserModel creator;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "avatar_url", columnDefinition = "TINYTEXT")
    private String avatarUrl;

    @Column(name = "cover_url", columnDefinition = "TINYTEXT")
    private String coverUrl;

    @Column(name = "website", columnDefinition = "TINYTEXT")
    private String website;

    @Column(name = "privacy", columnDefinition = "ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PUBLIC'")
    @Enumerated(EnumType.STRING)
    private Privacy privacy = Privacy.PUBLIC;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private Date updateAt;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private StatusPostModel status;

    public enum Privacy {
        PUBLIC, PRIVATE
    }
}
