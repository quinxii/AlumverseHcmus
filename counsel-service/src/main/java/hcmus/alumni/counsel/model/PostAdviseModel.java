package hcmus.alumni.counsel.model;

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
    private StatusPostModel status;

    @Column(name = "children_comment_number", columnDefinition = "INT DEFAULT(0)")
    private Integer childrenCommentNumber = 0;

    public PostAdviseModel(String id) {
        this.id = id;
    }
}
