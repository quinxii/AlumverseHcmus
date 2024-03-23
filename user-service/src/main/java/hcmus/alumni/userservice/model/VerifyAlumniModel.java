package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[verify_alumni]")
@Getter
@Setter
@AllArgsConstructor
public class VerifyAlumniModel implements Serializable {	
    public enum Status {
        PENDING,
        APPROVED,
        DENIED
    }
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", nullable = false)
	private String id;
	
    @ManyToOne // This establishes the ManyToOne relationship
    @JoinColumn(name = "user_id", nullable = false) // Foreign key constraint
    private UserModel user; // User object representing the related user

	@Column(name = "student_id", length = 8)
	private String studentId;

	@Column(name = "beginning_year")
	private Integer beginningYear;
	
	@Column(name = "social_media_link", length = 100)
	private String socialMediaLink;

	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private Status status;

	@CreationTimestamp
	@Column(name = "create_at", updatable = false)
	private Date createAt;

	@Column(name = "is_delete")
	private Boolean isDelete;
	
	public VerifyAlumniModel(UserModel user, String studentId, Integer beginningYear, String socialMediaLink) {
		super();
		this.id = UUID.randomUUID().toString();
		this.user = user;
		this.studentId = studentId;
		this.beginningYear = beginningYear;
		this.socialMediaLink = socialMediaLink;
		this.status = Status.PENDING;
		this.isDelete = false;
	}
	
	public VerifyAlumniModel() {
		super();
		this.id = UUID.randomUUID().toString();
		this.user = null;
		this.studentId = null;
		this.beginningYear = null;
		this.socialMediaLink = null;
		this.status = Status.PENDING;
		this.isDelete = false;
	}
}
