package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
            )
    private Set<RoleModel> roles = new HashSet<>();

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
    
    public UserModel() {}
    
    public UserModel(String email, String pass) {
    	this.email = email;
    	this.pass = pass;
	}
}
