package hcmus.alumni.event.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hcmus.alumni.event.common.CommentEventPermissions;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[comment_event]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentEventModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;
	
	@OneToOne
	@JoinColumn(name = "creator")
	private UserModel creator;
	
	@ManyToOne
	@JoinColumn(name = "event_id", nullable = false)
	private EventModel event;
	
	@Column(name = "parent_id", length = 36)
	private String parentId;
	
	@Column(name = "content", columnDefinition = "TEXT")
	private String content;
	
	@Column(name = "children_comment_number", columnDefinition = "INT DEFAULT(0)")
	private Integer childrenCommentNumber = 0;
	
	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;
	
	@Transient
	private CommentEventPermissions permissions;

	public CommentEventModel(String userId, String eventId, String parentId, String content) {
		this.id = UUID.randomUUID().toString();
		this.creator = new UserModel(userId);
		this.event = new EventModel(eventId);
		this.parentId = parentId;
		this.content = content;
	}
	
	public CommentEventModel(CommentEventModel copy, String userId, boolean canDelete) {
		this.id = copy.id;
		this.creator = copy.creator;
		this.event = copy.event;
		this.parentId = copy.parentId;
		this.content = copy.content;
		this.childrenCommentNumber = copy.childrenCommentNumber;
		this.createAt = copy.createAt;
		this.updateAt = copy.updateAt;
		this.isDelete = copy.isDelete;

		this.permissions = new CommentEventPermissions(false, false);
        if (copy.creator.getId().equals(userId)) {
            this.permissions.setDelete(true);
            this.permissions.setEdit(true);
        }
        if (canDelete) {
            this.permissions.setDelete(true);
        }
	}
}
