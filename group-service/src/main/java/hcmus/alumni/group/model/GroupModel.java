package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hcmus.alumni.group.common.Privacy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "`group`")
@AllArgsConstructor
@NoArgsConstructor
@Data
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
    private StatusUserGroupModel status;
    
    @Column(name = "participant_count")
	private Integer participantCount;
    
    @Transient
	private Set<UserModel> joinedFriends;
    
    @Transient
	private boolean isJoined;
    
    @Transient
	private boolean isRequestPending;
    
    public GroupModel(String id) {
    	this.id = id;
    }
}
