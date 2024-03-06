package hcmus.alumni.userservice.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import hcmus.alumni.userservice.utils.Privacy;

@Entity
@Table(name = "user")
public class UserModel {
    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "pass", nullable = false)
    private String pass;

    @Column(name = "roleId", nullable = false)
    private String roleId;

    @Column(name = "fullName")
    private String fullName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "sexId", nullable = false)
    private String sexId;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "aboutMe", columnDefinition = "TEXT")
    private String aboutMe;

    @Column(name = "avatarUrl")
    private String avatarUrl;

    @Column(name = "coverUrl")
    private String coverUrl;

    @Column(name = "statusId", nullable = false)
    private String statusId;

    @Column(name = "createAt")
    private Date createAt;

    @Column(name = "updateAt")
    private Date updateAt;

    @Column(name = "lastLogin")
    private Date lastLogin;

    @Column(name = "onlineStatus", nullable = false)
    private boolean onlineStatus;

    @Column(name = "emailPrivacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy emailPrivacy;

    @Column(name = "phonePrivacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy phonePrivacy;

    @Column(name = "sexPrivacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy sexPrivacy;

    @Column(name = "dobPrivacy", nullable = false)
    @Enumerated(EnumType.STRING)
    private Privacy dobPrivacy;

    	
    // Constructors, getters, and setters
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
    
    
}

