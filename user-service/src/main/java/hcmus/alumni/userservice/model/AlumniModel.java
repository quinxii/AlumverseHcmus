package hcmus.alumni.userservice.model;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import hcmus.alumni.userservice.common.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "[alumni]")
@AllArgsConstructor
@Data
public class AlumniModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId; 
    
    @OneToOne
    @PrimaryKeyJoinColumn(name = "user_id")
    private UserModel user;

    @Column(name = "student_id", length = 8)
    private String studentId;

    @Column(name = "beginning_year")
    private Integer beginningYear;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "class", length = 10)
    private String alumClass;

    @Column(name = "student_id_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy studentIdPrivacy;

    @Column(name = "start_year_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy startYearPrivacy;

    @Column(name = "graduation_year_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy endYearPrivacy;

    @Column(name = "class_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy classPrivacy;

    public AlumniModel() {
    	studentIdPrivacy = Privacy.PUBLIC;
    	startYearPrivacy = Privacy.PUBLIC;
    	endYearPrivacy = Privacy.PUBLIC;
    	classPrivacy = Privacy.PUBLIC;
	}
    
}