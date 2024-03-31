package hcmus.alumni.events.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[news]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewsModel implements Serializable {
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id; 
    
    @ManyToOne
    @JoinColumn(name = "creator", nullable = false)
    private UserModel creator;

    @Column(name = "title", columnDefinition = "TINITEXT")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

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
    private StatusPost status;

    @Column(name = "views", nullable = false)
    private Integer views = 0;
}
