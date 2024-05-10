package hcmus.alumni.counsel.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    @ManyToOne
    @JoinColumn(name = "creator", nullable = false)
    private UserModel creator;

    @Column(name = "title", columnDefinition = "TINYTEXT")
    private String title;

    @OrderBy("pitctureOrder ASC")
    @OneToMany(mappedBy = "postAdvise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PicturePostAdviseModel> pictures;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "tag_post_advise", joinColumns = @JoinColumn(name = "post_advise_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
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
    private StatusPostModel status = new StatusPostModel(2);

    @Column(name = "children_comment_number", columnDefinition = "INT DEFAULT(0)")
    private Integer childrenCommentNumber = 0;

    @Column(name = "reaction_count", columnDefinition = "INT DEFAULT(0)")
    private Integer reactionCount = 0;

    @Transient
    private boolean isReacted;

    public PostAdviseModel() {
    }

    public PostAdviseModel(String id) {
        this.id = id;
    }

    // For request body
    @JsonCreator
    public PostAdviseModel(@JsonProperty("title") String title, @JsonProperty("content") String content,
            @JsonProperty("tags") Set<TagModel> tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public PostAdviseModel(String creator, String title, String content, Set<TagModel> tags) {
        this.id = java.util.UUID.randomUUID().toString();
        this.creator = new UserModel(creator);
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public PostAdviseModel(PostAdviseModel copy, Boolean isReactionDelete) {
        this.id = copy.id;
        this.creator = copy.creator;
        this.title = copy.title;
        this.pictures = copy.pictures;
        this.content = copy.content;
        this.tags = copy.tags;
        this.createAt = copy.createAt;
        this.updateAt = copy.updateAt;
        this.publishedAt = copy.publishedAt;
        this.status = copy.status;
        this.childrenCommentNumber = copy.childrenCommentNumber;
        this.reactionCount = copy.reactionCount;
        if (isReactionDelete != null) {
            this.isReacted = !isReactionDelete;
        } else {
            this.isReacted = false;
        }
    }

    public void addPicture(PicturePostAdviseModel picture) {
        this.pictures.add(picture);
    }
}
