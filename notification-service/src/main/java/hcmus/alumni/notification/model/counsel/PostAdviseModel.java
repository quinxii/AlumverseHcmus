package hcmus.alumni.notification.model.counsel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "[post_advise]")
@AllArgsConstructor
@Data
public class PostAdviseModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;
	
	@Column(name = "creator", length = 36, nullable = false)
	private String creator;
	
	@Column(name = "title", columnDefinition = "TINYTEXT")
	private String title;
	
	@Column(name = "content", columnDefinition = "TEXT")
	private String content;
	
	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;
	
	@CreationTimestamp
	@Column(name = "published_at")
	private Date publishedAt;
	
	@Column(name = "status_id")
	private Integer statusId;
	
	@Column(name = "children_comment_number", columnDefinition = "INT DEFAULT(0)")
	private Integer childrenCommentNumber = 0;
	
	@Column(name = "reaction_count", columnDefinition = "INT DEFAULT(0)")
	private Integer reactionCount = 0;
	
	@Column(name = "allow_multiple_votes", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean allowMultipleVotes = false;
	
	@Column(name = "allow_add_options", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean allowAddOptions = false;
}
