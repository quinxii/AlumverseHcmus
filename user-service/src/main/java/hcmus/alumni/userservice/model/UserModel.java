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
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import hcmus.alumni.userservice.common.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "[user]")
@AllArgsConstructor
@Data
public class UserModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "pass", length = 60, nullable = false)
    private String pass;

	@OneToOne
    @JoinColumn(name = "role_id", nullable = false) // Foreign key constraint
	private RoleModel role;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 15)
    private String phone;

	@OneToOne
    @JoinColumn(name = "sex_id") // Foreign key constraint
	private SexModel sex;

    @Column(name = "dob")
    private Date dob;
    
    @Column(name = "social_media_link", columnDefinition = "TINYTEXT")
    private String socialMediaLink;
    
	@OneToOne
    @JoinColumn(name = "faculty_id") // Foreign key constraint
	private FacultyModel faculty;
    
    @Column(name = "degree", length = 50)
    private String degree;
    
    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe;

    @Column(name = "avatar_url", columnDefinition = "TINYTEXT")
    private String avatarUrl;

    @Column(name = "cover_url", columnDefinition = "TINYTEXT")
    private String coverUrl;

    @Column(name = "status_id")
    private Integer statusId;

	@CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "update_at")
    private Date updateAt;

    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "online_status")
    private boolean onlineStatus;

    @Column(name = "email_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy emailPrivacy;

    @Column(name = "phone_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy phonePrivacy;

    @Column(name = "sex_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy sexPrivacy;

    @Column(name = "dob_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy dobPrivacy;
    
    @Column(name = "faculty_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
    @Enumerated(EnumType.STRING)
    private Privacy facultyPrivacy;
    
    public UserModel() {
    	id = UUID.randomUUID().toString();
    	role = new RoleModel(1);
    	emailPrivacy = Privacy.PUBLIC;
    	phonePrivacy = Privacy.PUBLIC;
    	sexPrivacy = Privacy.PUBLIC;
    	dobPrivacy = Privacy.PUBLIC;
    	facultyPrivacy = Privacy.PUBLIC;
	}
    
    public UserModel(String email, String pass) {
    	id = UUID.randomUUID().toString();
    	this.email = email;
    	this.pass = pass;
    	role = new RoleModel(1);
    	emailPrivacy = Privacy.PUBLIC;
    	phonePrivacy = Privacy.PUBLIC;
    	sexPrivacy = Privacy.PUBLIC;
    	dobPrivacy = Privacy.PUBLIC;
    	facultyPrivacy = Privacy.PUBLIC;
	}
}
