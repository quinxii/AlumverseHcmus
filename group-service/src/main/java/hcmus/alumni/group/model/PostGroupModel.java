package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    
    @OrderBy("pitctureOrder ASC")
    @OneToMany(mappedBy = "postGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PicturePostGroupModel> pictures;

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
    
    public PostGroupModel(String id) {
		this.id = id;
	}
    
    public PostGroupModel(String groupid, String creator, String title, String content, Set<TagModel> tags, StatusPostModel status) {
        this.id = java.util.UUID.randomUUID().toString();
        this.groupId = groupid;
        this.creator = new UserModel(creator);
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.status = status;
    }
    
    public void addPicture(PicturePostGroupModel picture) {
        this.pictures.add(picture);
    }
}

