package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import hcmus.alumni.userservice.common.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[achievement]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AchievementModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "achievement_id", length = 36, nullable = false)
    private String achievementId; 

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

	@Column(name = "achievement_name", length = 255, nullable = false)
	private String achievementName;

    @Column(name = "achievement_type", length = 50, nullable = false)
	private String achievementType;

	@Column(name = "achievement_time")
	private Date achievementTime;

    @Column(name = "privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy privacy;

    @CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;

}
