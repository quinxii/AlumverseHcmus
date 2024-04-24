package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "post_group")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostGroupModel implements Serializable {
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

    @Column(name = "group_id", length = 36)
    private String groupId;
    
    @ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(
	    name = "tag_post_group",
	    joinColumns = @JoinColumn(name = "post_group_id"),
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

    @Column(name = "children_comment_number", columnDefinition = "INT DEFAULT 0")
    private Integer childrenCommentNumber = 0;
    
    public void setTags(Integer[] tags) {
		Set<TagModel> newTags = new HashSet<>();
		for (Integer tag : tags) {
			newTags.add(new TagModel(tag));
		}
		this.tags = newTags;
	}
    
    public PostGroupModel(String id) {
		this.id = id;
	}
}

