package hcmus.alumni.authservice.model;

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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[user]")
@AllArgsConstructor
@Data
public class UserModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Privacy {
	    PUBLIC,
	    FRIEND,
	    ONLYME
	}

	@Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "pass", nullable = false)
    private String pass;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "sex_id")
    private Integer sexId;

    @Column(name = "dob")
    private Date dob;
    
    @Column(name = "social_media_link", columnDefinition = "TINYTEXT")
    private String socialMediaLink;
    
    @Column(name = "faculty_id")
    private Integer facultyId;
    
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
    	roleId = 1;
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
    	roleId = 1;
    	emailPrivacy = Privacy.PUBLIC;
    	phonePrivacy = Privacy.PUBLIC;
    	sexPrivacy = Privacy.PUBLIC;
    	dobPrivacy = Privacy.PUBLIC;
    	facultyPrivacy = Privacy.PUBLIC;
	}
}
