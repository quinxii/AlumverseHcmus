package hcmus.alumni.userservice.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[sex]")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SexModel implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false, columnDefinition = "TINYINT")
    private Integer id;
	
	@Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;
	
	@Column(name = "description", columnDefinition = "TINYTEXT")
    private String description;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
    private Boolean isDelete;
	
	public SexModel(Integer id) {
		this.id = id;
	}
}