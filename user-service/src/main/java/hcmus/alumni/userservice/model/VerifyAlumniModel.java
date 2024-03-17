package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "[verify_alumni]")
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
	
	@Column(name = "user_id", nullable = false)
	private String userId;

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
	private Date createdAt;

	@Column(name = "is_delete")
	private Boolean isDelete;
	
	public VerifyAlumniModel(String user_id, String studentId, Integer beginningYear, String socialMediaLink) {
		super();
		this.id = UUID.randomUUID().toString();
		this.userId = user_id;
		this.studentId = studentId;
		this.beginningYear = beginningYear;
		this.socialMediaLink = socialMediaLink;
		this.status = Status.PENDING;
		this.isDelete = false;
	}
	
	public VerifyAlumniModel() {
		super();
		this.id = UUID.randomUUID().toString();
		this.userId = null;
		this.studentId = null;
		this.beginningYear = null;
		this.socialMediaLink = null;
		this.status = Status.PENDING;
		this.isDelete = false;
	}
	

	public String getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public int getBeginningYear() {
		return beginningYear;
	}

	public void setBeginningYear(Integer beginningYear) {
		this.beginningYear = beginningYear;
	}
	
	public String getSocialMediaLink() {
		return socialMediaLink;
	}

	public void setSocialMediaLink(String socialMediaLink) {
		this.socialMediaLink = socialMediaLink;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Boolean getIsDelete() {
		return isDelete;
	}

	public void setIsDelete(Boolean isDelete) {
		this.isDelete = isDelete;
	}

	@Override
	public String toString() {
		return "VerifyAlumniModel [id=" + id + ", userId=" + userId + ", studentId=" + studentId + ", beginningYear="
				+ beginningYear + ", socialMediaLink=" + socialMediaLink + ", comment=" + comment + ", status=" + status
				+ ", createdAt=" + createdAt + ", isDelete=" + isDelete + "]";
	}
}