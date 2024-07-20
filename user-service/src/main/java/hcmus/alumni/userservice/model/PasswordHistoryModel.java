package hcmus.alumni.userservice.model;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "[password_history]")
@AllArgsConstructor
@Data
public class PasswordHistoryModel {
	@Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "is_auto_generated", nullable = false)
    private boolean isAutoGenerated;

    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;
    
    @UpdateTimestamp	
    @Column(name = "updated_at")
    private Date updatedAt;

    public PasswordHistoryModel() {
        this.id = UUID.randomUUID().toString();
    }

    public PasswordHistoryModel(String userId, String password, boolean isAutoGenerated, Date createdAt, Date updatedAt) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.password = password;
        this.isAutoGenerated = isAutoGenerated;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public PasswordHistoryModel(String userId, String password, boolean isAutoGenerated, Date updatedAt) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.password = password;
        this.isAutoGenerated = isAutoGenerated;
        this.updatedAt = updatedAt;
    }
}
