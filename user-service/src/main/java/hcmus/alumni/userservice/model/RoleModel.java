package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import hcmus.alumni.userservice.dto.RoleRequestDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[role]")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, columnDefinition = "TINYINT")
	private Integer id;

	@Column(name = "name", length = 100, nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 100)
	private String description;

	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;

	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;

	@Getter(value = AccessLevel.NONE)
	@OrderBy("id ASC")
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "role_permission", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
	private Set<PermissionModel> permissions = new HashSet<>();

	public RoleModel(Integer id) {
		this.id = id;
	}

	public RoleModel(RoleRequestDto role) {
		this.name = role.getName();
		this.description = role.getDescription();
		role.getPermissions().forEach(permission -> {
			this.permissions.add(new PermissionModel(permission.getId()));
		});
	}

	public RoleModel(RoleRequestDto role, Integer id) {
		this.id = id;
		this.name = role.getName();
		this.description = role.getDescription();
		role.getPermissions().forEach(permission -> {
			this.permissions.add(new PermissionModel(permission.getId()));
		});
	}

	public void clearPermissions() {
		this.permissions.clear();
	}

	public void addPermission(PermissionModel permission) {
		this.permissions.add(permission);
	}

	public void addPermissions(List<PermissionModel> permissions) {
		this.permissions.addAll(permissions);
	}

	public void removePermission(PermissionModel permission) {
		this.permissions.remove(permission);
	}
}