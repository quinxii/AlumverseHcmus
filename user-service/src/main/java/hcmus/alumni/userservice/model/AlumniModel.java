package hcmus.alumni.userservice.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

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

    @Column(name = "specialized")
    private String specialized;

    @Column(name = "class", length = 10)
    private String alumClass;

    @Column(name = "student_id_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy studentIdPrivacy;

    @Column(name = "start_year_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy startYearPrivacy;

    @Column(name = "end_year_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy endYearPrivacy;

    @Column(name = "specialized_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy specializedPrivacy;

    @Column(name = "class_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy classPrivacy;

    public AlumniModel() {
    	studentIdPrivacy = Privacy.PUBLIC;
    	startYearPrivacy = Privacy.PUBLIC;
    	endYearPrivacy = Privacy.PUBLIC;
    	specializedPrivacy = Privacy.PUBLIC;
    	classPrivacy = Privacy.PUBLIC;
	}
}