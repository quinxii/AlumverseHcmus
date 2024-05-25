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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

import hcmus.alumni.group.common.GroupMemberRole;
import hcmus.alumni.group.common.GroupPermissions;

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
    
    @Column(name = "description", columnDefinition = "TINYTEXT")
    private String description;

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
	private GroupMemberRole userRole;
    
    @Transient
	private boolean isRequestPending;
    
    @Transient
	private GroupPermissions permissions;
    
    public GroupModel(String id) {
    	this.id = id;
    }
    
    public GroupModel(GroupModel copy, GroupMemberRole userRole, boolean isRequestPending, String userId, boolean canDelete) {
        this.id = copy.getId();
        this.name = copy.getName();
        this.creator = copy.getCreator();
        this.description = copy.getDescription();
        this.type = copy.getType();
        this.coverUrl = copy.getCoverUrl();
        this.website = copy.getWebsite();
        this.privacy = copy.getPrivacy();
        this.createAt = copy.getCreateAt();
        this.updateAt = copy.getUpdateAt();
        this.status = copy.getStatus();
        this.participantCount = copy.getParticipantCount();
        this.userRole = userRole;
        this.isRequestPending = isRequestPending;
        
        this.permissions = new GroupPermissions(false, false);
        if (copy.creator.getId().equals(userId)) {
            this.permissions.setDelete(true);
            this.permissions.setEdit(true);
        }
        if (canDelete) {
            this.permissions.setDelete(true);
        }
    }
}
