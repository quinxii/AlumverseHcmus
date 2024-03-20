package hcmus.alumni.authservice.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "[user]")
public class UserModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Privacy {
	    PUBLIC,
	    FRIEND,
	    ONLY_ME
	}
	
	public enum Role {
	    GUEST,
	    ALUMNI,
	    TEACHER,
	    MINISTRY,
	    ADMIN,
	    OTHER
	}
	
	public enum Sex {
	    MALE,
	    FEMALE,
	    OTHER
	}

	@Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "pass", nullable = false)
    private String pass;

    @Column(name = "role_id", nullable = false)
    private String roleId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "sex_id", nullable = false)
    private String sexId;

    @Column(name = "dob")
    private Date dob;
    
    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "status_id", nullable = false)
    private String statusId;

    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "update_at")
    private Date updateAt;

    @Column(name = "last_login")
    private Date lastLogin;

    @Column(name = "online_status", nullable = false)
    private boolean onlineStatus;

    @Column(name = "email_privacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy emailPrivacy;

    @Column(name = "phone_privacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy phonePrivacy;

    @Column(name = "sex_privacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy sexPrivacy;

    @Column(name = "dob_privacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy dobPrivacy;


    // Constructors, getters, and setters
    public UserModel(String id, String email, String pass, String roleId, String fullName, String phone, String sexId,
    		Date dob, String aboutMe, String avatarUrl, String coverUrl, String statusId, Date createAt, Date updateAt,
    		Date lastLogin, boolean onlineStatus, Privacy emailPrivacy, Privacy phonePrivacy, Privacy sexPrivacy,
    		Privacy dobPrivacy) {
    	super();
    	this.id = id;
    	this.email = email;
    	this.pass = pass;
    	this.roleId = roleId;
    	this.fullName = fullName;
    	this.phone = phone;
    	this.sexId = sexId;
    	this.dob = dob;
    	this.aboutMe = aboutMe;
    	this.avatarUrl = avatarUrl;
    	this.coverUrl = coverUrl;
    	this.statusId = statusId;
    	this.createAt = createAt;
    	this.updateAt = updateAt;
    	this.lastLogin = lastLogin;
    	this.onlineStatus = onlineStatus;
    	this.emailPrivacy = emailPrivacy;
    	this.phonePrivacy = phonePrivacy;
    	this.sexPrivacy = sexPrivacy;
    	this.dobPrivacy = dobPrivacy;
    }
    
    
	public UserModel() {
		this.id = "";
    	this.email = "";
    	this.pass = "";
    	this.roleId = "";
    	this.fullName = "";
    	this.phone = "";
    	this.sexId = "";
    	this.dob = null;
    	this.aboutMe = "";
    	this.avatarUrl = "";
    	this.coverUrl = "";
    	this.statusId = "";
    	this.createAt = null;
    	this.updateAt = null;
    	this.lastLogin = null;
    	this.onlineStatus = false;
    	this.emailPrivacy = null;
    	this.phonePrivacy = null;
    	this.sexPrivacy = null;
    	this.dobPrivacy = null;
	}



	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSexId() {
		return sexId;
	}

	public void setSexId(String sexId) {
		this.sexId = sexId;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}

	public String getAboutMe() {
		return aboutMe;
	}

	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	public String getStatusId() {
		return statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public Date getUpdateAt() {
		return updateAt;
	}

	public void setUpdateAt(Date updateAt) {
		this.updateAt = updateAt;
	}

	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public boolean isOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(boolean onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public Privacy getEmailPrivacy() {
		return emailPrivacy;
	}

	public void setEmailPrivacy(Privacy emailPrivacy) {
		this.emailPrivacy = emailPrivacy;
	}

	public Privacy getPhonePrivacy() {
		return phonePrivacy;
	}

	public void setPhonePrivacy(Privacy phonePrivacy) {
		this.phonePrivacy = phonePrivacy;
	}

	public Privacy getSexPrivacy() {
		return sexPrivacy;
	}

	public void setSexPrivacy(Privacy sexPrivacy) {
		this.sexPrivacy = sexPrivacy;
	}

	public Privacy getDobPrivacy() {
		return dobPrivacy;
	}

	public void setDobPrivacy(Privacy dobPrivacy) {
		this.dobPrivacy = dobPrivacy;
	}

	@Override
	public String toString() {
		return "UserModel [id=" + id + ", email=" + email + ", pass=" + pass + ", roleId=" + roleId + ", fullName="
				+ fullName + ", phone=" + phone + ", sexId=" + sexId + ", dob=" + dob + ", aboutMe=" + aboutMe
				+ ", avatarUrl=" + avatarUrl + ", coverUrl=" + coverUrl + ", statusId=" + statusId + ", createAt="
				+ createAt + ", updateAt=" + updateAt + ", lastLogin=" + lastLogin + ", onlineStatus=" + onlineStatus
				+ ", emailPrivacy=" + emailPrivacy + ", phonePrivacy=" + phonePrivacy + ", sexPrivacy=" + sexPrivacy
				+ ", dobPrivacy=" + dobPrivacy + "]";
	}
}
