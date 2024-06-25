package hcmus.alumni.notification.model.counsel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "[comment_post_advise]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommentPostAdviseModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;
	
	@Column(name = "creator", length = 36, nullable = false)
	private String creator;
	
	@Column(name = "post_advise_id", length = 36, nullable = false)
	private String postAdviseId;
	
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
}
