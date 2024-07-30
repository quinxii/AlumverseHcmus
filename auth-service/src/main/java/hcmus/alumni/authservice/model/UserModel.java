package hcmus.alumni.authservice.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

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
    private Integer statusId = 2;

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
    
    public UserModel() {
    	id = UUID.randomUUID().toString();
    	roles.add(new RoleModel(5));
	}
    
    public UserModel(String email, String pass) {
    	id = UUID.randomUUID().toString();
    	this.email = email;
    	this.pass = pass;
    	roles.add(new RoleModel(5));
	}
    
    public ArrayList<String> getRolesName() {
    	ArrayList<String> rolesName = new ArrayList<String>();
    	for (RoleModel role : roles) {
    		rolesName.add(role.getName());
    	}
    	return rolesName;
    }
}