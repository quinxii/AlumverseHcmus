package hcmus.alumni.notification.model.user;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[user]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Privacy {
		PUBLIC, FRIEND, ONLYME
	}
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;
	
	@Column(name = "email", length = 255, nullable = false, unique = true)
	private String email;
	
	@Column(name = "pass", length = 60, nullable = false)
	private String pass;
	
	@Column(name = "full_name", length = 100)
	private String fullName;
	
	@Column(name = "phone", length = 15)
	private String phone;
	
	@Column(name = "sex_id")
	private Integer sex;
	
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
	private Privacy emailPrivacy = Privacy.PUBLIC;
	
	@Column(name = "phone_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
	@Enumerated(EnumType.STRING)
	private Privacy phonePrivacy = Privacy.PUBLIC;
	
	@Column(name = "sex_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
	@Enumerated(EnumType.STRING)
	private Privacy sexPrivacy = Privacy.PUBLIC;
	
	@Column(name = "dob_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
	@Enumerated(EnumType.STRING)
	private Privacy dobPrivacy = Privacy.PUBLIC;
	
	@Column(name = "faculty_privacy", columnDefinition = "ENUM('PUBLIC', 'FRIEND', 'ONLYME') DEFAULT('PUBLIC')")
	@Enumerated(EnumType.STRING)
	private Privacy facultyPrivacy = Privacy.PUBLIC;
}