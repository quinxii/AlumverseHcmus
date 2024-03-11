package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "[verify_alumni]")
public class VerifyAlumniModel implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id", nullable = false)
	private String id;

	@Column(name = "student_id", length = 8)
	private String studentId;

	@Column(name = "beginning_year")
	private int beginningYear;

	@Column(name = "comment", columnDefinition = "TEXT")
	private String comment;

	@Column(name = "status")
	private Boolean status;

	@Column(name = "create_at", nullable = false, updatable = false)
	private Date createdAt;

	@Column(name = "is_delete", nullable = false)
	private Boolean isDelete;

	public VerifyAlumniModel(String id, String studentId, int beginningYear, String comment, Boolean status,
			Date createdAt, Boolean isDelete) {
		super();
		this.id = id;
		this.studentId = studentId;
		this.beginningYear = beginningYear;
		this.comment = comment;
		this.status = status;
		this.createdAt = createdAt;
		this.isDelete = isDelete;
	}
	
	public VerifyAlumniModel() {
		super();
		this.id = "";
		this.studentId = "";
		this.beginningYear = 0;
		this.comment = "";
		this.status = false;
		this.createdAt = null;
		this.isDelete = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setBeginningYear(int beginningYear) {
		this.beginningYear = beginningYear;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
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

}
