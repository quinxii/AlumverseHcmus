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
@Table(name = "[education]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EducationModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "education_id", length = 36, nullable = false)
    private String educationId; 

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

	@Column(name = "school_name", length = 255, nullable = false)
	private String schoolName;

    @Column(name = "degree", length = 100, nullable = false)
	private String degree;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "end_time")
	private Date endTime;

    @Column(name = "privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy privacy;

    @CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;

    @Column(name = "is_learning", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isLearning;
}
