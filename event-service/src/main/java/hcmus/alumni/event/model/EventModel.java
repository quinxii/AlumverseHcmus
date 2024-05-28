package hcmus.alumni.event.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[event]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EventModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private UserModel creator;
	
	@Column(name = "title", columnDefinition = "TINYTEXT")
	private String title;
	
	@Column(name = "content", columnDefinition = "TEXT")
	private String content;
	
	@Column(name = "thumbnail", columnDefinition = "TINYTEXT")
	private String thumbnail;
	
	@Column(name = "organization_location", columnDefinition = "TINYTEXT")
	private String organizationLocation;
	
	@Column(name = "organization_time")
	private Date organizationTime;
	
	@OneToOne
	@JoinColumn(name = "faculty_id")
	private FacultyModel faculty;
    
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
	    name = "tag_event",
	    joinColumns = @JoinColumn(name = "event_id"),
	    inverseJoinColumns = @JoinColumn(name = "tag_id")
	)
	private Set<TagModel> tags = new HashSet<>();

	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;
	
	@Column(name = "published_at")
	private Date publishedAt;
	
	@ManyToOne
	@JoinColumn(name = "status_id")
	private StatusPostModel status;
	
	@Column(name = "views", nullable = false)
	private Integer views = 0;
	
	@Column(name = "participants", columnDefinition = "INT DEFAULT(0)")
	private Integer participants = 0;
	
	@Column(name = "minimum_participants", columnDefinition = "INT DEFAULT(0)")
	private Integer minimumParticipants = 0;
	
	@Column(name = "maximum_participants", columnDefinition = "INT DEFAULT(0)")
	private Integer maximumParticipants = 0;
	
	@Column(name = "children_comment_number", columnDefinition = "INT DEFAULT(0)")
	private Integer childrenCommentNumber = 0;
	
	public void setTags(Integer[] tags) {
		Set<TagModel> newTags = new HashSet<>();
		for (Integer tag : tags) {
			newTags.add(new TagModel(tag));
		}
		this.tags = newTags;
	}
	
	public EventModel(String id) {
		this.id = id;
	}
}