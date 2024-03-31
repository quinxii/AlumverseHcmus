package hcmus.alumni.halloffame.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hall_of_fame")
@AllArgsConstructor
@NoArgsConstructor
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
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "update_at")
    private Date updatedAt;
    
    @Column(name = "published_at")
    private Date publishedAt;

    @Column(name = "status_id")
    private StatusPost statusId;

    @Column(name = "views", nullable = false)
    private int views = 0;

}
