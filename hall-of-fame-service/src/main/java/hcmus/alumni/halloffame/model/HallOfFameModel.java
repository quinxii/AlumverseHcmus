package hcmus.alumni.halloffame.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "hall_of_fame")
@AllArgsConstructor
@Data
public class HallOfFameModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;
    
    @Column(name = "creator", length = 36, nullable = false)
    private UserModel creator;

    @Column(name = "title", columnDefinition = "TINYTEXT")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail", columnDefinition = "TINYTEXT")
    private String thumbnail;

    @Column(name = "user_id", length = 36)
    private UserModel userId;

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

    @Column(name = "faculty", columnDefinition = "TINYTEXT")
    private String faculty;

    @Column(name = "beginning_year")
    private Integer beginningYear;
    
    public HallOfFameModel() {
		id = UUID.randomUUID().toString();
		status = new StatusPostModel(2);
	}
    
	public HallOfFameModel(String id, UserModel creator, String title, String content, String thumbnail, String faculty, int beginningYear) {
		this.id = id;
		this.creator = creator;
		this.title = title;
		this.content = content;
		this.thumbnail = thumbnail;
		this.faculty = faculty;
		this.beginningYear = beginningYear;
		this.status = new StatusPostModel(3);
	}
}
