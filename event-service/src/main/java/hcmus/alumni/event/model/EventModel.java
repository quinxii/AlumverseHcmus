package hcmus.alumni.event.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[event]")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EventModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	@EqualsAndHashCode.Include
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
    
	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
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

	@Transient
	boolean isParticipated;
	
	public EventModel(String id) {
		this.id = id;
	}

	public EventModel(EventModel copy, boolean isParticipated) {
		this.id = copy.id;
		this.creator = copy.creator;
		this.title = copy.title;
		this.content = copy.content;
		this.thumbnail = copy.thumbnail;
		this.organizationLocation = copy.organizationLocation;
		this.organizationTime = copy.organizationTime;
		this.faculty = copy.faculty;
		this.tags = new HashSet<>(copy.tags);
		this.createAt = copy.createAt;
		this.updateAt = copy.updateAt;
		this.publishedAt = copy.publishedAt;
		this.status = copy.status;
		this.views = copy.views;
		this.participants = copy.participants;
		this.minimumParticipants = copy.minimumParticipants;
		this.maximumParticipants = copy.maximumParticipants;
		this.childrenCommentNumber = copy.childrenCommentNumber;
		this.isParticipated = isParticipated;
	}
}