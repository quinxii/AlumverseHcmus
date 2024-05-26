package hcmus.alumni.event.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import hcmus.alumni.event.common.ParticipantEventPermissions;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[participant_event]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParticipantEventModel {
	@EmbeddedId
    private ParticipantEventId id;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createdAt;

    @Column(name = "is_delete")
    private boolean isDelete;
    
    @Transient
	private String fullName;
    
    @Transient
	private String avatarUrl;
    
    @Transient
	private ParticipantEventPermissions permissions;
    
    public ParticipantEventModel(ParticipantEventModel copy, String fullName, String avatarUrl, String userId, boolean canDelete) {
    	this.id = copy.id;
    	this.note = copy.note;
    	this.createdAt = copy.createdAt;
    	this.isDelete = copy.isDelete;
    	this.fullName = fullName;
    	this.avatarUrl = avatarUrl;
    	
    	this.permissions = new ParticipantEventPermissions(false);
    	if (this.id.getUserId().equals(userId)|| canDelete) {
            this.permissions.setDelete(true);
        }
    }
}

