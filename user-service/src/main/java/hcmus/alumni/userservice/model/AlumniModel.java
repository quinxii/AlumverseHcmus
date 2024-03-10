package hcmus.alumni.userservice.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "[alumni]")
public class AlumniModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId; 

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "beginning_year")
    private int beginningYear;

    @Column(name = "graduation_year")
    private int graduationYear;

    @Column(name = "specialized")
    private String specialized;

    @Column(name = "class")
    private String alumClass;

    @Column(name = "student_id_privacy")
    private String studentIdPrivacy;

    @Column(name = "start_year_privacy")
    private String startYearPrivacy;

    @Column(name = "end_year_privacy")
    private String endYearPrivacy;

    @Column(name = "specialized_privacy")
    private String specializedPrivacy;

    @Column(name = "class_privacy")
    private String classPrivacy;


    // Getters and setters
    
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

	public void setBeginningYear(int beginningYear) {
		this.beginningYear = beginningYear;
	}

	public int getGraduationYear() {
		return graduationYear;
	}

	public void setGraduationYear(int graduationYear) {
		this.graduationYear = graduationYear;
	}

	public String getSpecialized() {
		return specialized;
	}

	public void setSpecialized(String specialized) {
		this.specialized = specialized;
	}

	public String getAlumClass() {
		return alumClass;
	}

	public void setAlumClass(String alumClass) {
		this.alumClass = alumClass;
	}

	public String getStudentIdPrivacy() {
		return studentIdPrivacy;
	}

	public void setStudentIdPrivacy(String studentIdPrivacy) {
		this.studentIdPrivacy = studentIdPrivacy;
	}

	public String getStartYearPrivacy() {
		return startYearPrivacy;
	}

	public void setStartYearPrivacy(String startYearPrivacy) {
		this.startYearPrivacy = startYearPrivacy;
	}

	public String getEndYearPrivacy() {
		return endYearPrivacy;
	}

	public void setEndYearPrivacy(String endYearPrivacy) {
		this.endYearPrivacy = endYearPrivacy;
	}

	public String getSpecializedPrivacy() {
		return specializedPrivacy;
	}

	public void setSpecializedPrivacy(String specializedPrivacy) {
		this.specializedPrivacy = specializedPrivacy;
	}

	public String getClassPrivacy() {
		return classPrivacy;
	}

	public void setClassPrivacy(String classPrivacy) {
		this.classPrivacy = classPrivacy;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
