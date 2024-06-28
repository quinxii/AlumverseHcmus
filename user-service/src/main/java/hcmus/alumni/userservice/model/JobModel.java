package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import hcmus.alumni.userservice.common.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[job]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class JobModel implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId; 

	@Column(name = "company_name", length = 255, nullable = false)
	private String companyName;

    @Column(name = "position", length = 100, nullable = false)
	private String position;

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
	private Boolean isDelete;

    @Column(name = "is_working", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isWorking;
}
