package hcmus.alumni.userservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[status_user_group]")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusUserGroupModel implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, columnDefinition = "TINYINT")
    private Integer id;
	
	@Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
	
	@Column(name = "description", columnDefinition = "TINYTEXT")
    private String description;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete;
}

