package hcmus.alumni.authservice.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[role]")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleModel implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, columnDefinition = "TINYINT")
	private Integer id;

	@Column(name = "name", length = 100, nullable = false, unique = true)
	private String name;

	@Column(name = "description", columnDefinition = "TINYTEXT")
	private String description;

	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;

	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;

	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;

	public RoleModel(Integer id) {
		this.id = id;

	}
}
