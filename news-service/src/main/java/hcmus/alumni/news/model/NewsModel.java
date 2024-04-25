package hcmus.alumni.news.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "[news]")
@AllArgsConstructor
@Data
public class NewsModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;

	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private UserModel creator;

	@Column(name = "title", columnDefinition = "TINYTEXT")
	private String title;
	
	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "thumbnail", columnDefinition = "TINYTEXT")
	private String thumbnail;

	@OneToOne
	@JoinColumn(name = "faculty_id")
	private FacultyModel faculty;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "tag_news", joinColumns = @JoinColumn(name = "news_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
	private Set<TagModel> tags = new HashSet<>();

	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;

	@Column(name = "published_at")
	private Date publishedAt;

	@OneToOne
	@JoinColumn(name = "status_id")
	private StatusPostModel status;

	@Column(name = "views", nullable = false)
	private Integer views = 0;

	@Column(name = "children_comment_number", columnDefinition = "INT DEFAULT(0)")
	private Integer childrenCommentNumber = 0;

	public NewsModel() {
		id = UUID.randomUUID().toString();
		status = new StatusPostModel(2);
	}

	public NewsModel(String id) {
		this.id = id;
	}

	public NewsModel(String id, UserModel creator, String title, String summary, String content, String thumbnail) {
		this.id = id;
		this.creator = creator;
		this.title = title;
		this.summary = summary;
		this.content = content;
		this.thumbnail = thumbnail;
		this.status = new StatusPostModel(2);
	}

	public void setTags(Integer[] tags) {
		Set<TagModel> newTags = new HashSet<>();
		for (Integer tag : tags) {
			newTags.add(new TagModel(tag));
		}
		this.tags = newTags;
	}
}