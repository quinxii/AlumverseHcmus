package hcmus.alumni.userservice.model.notification;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.io.Serializable;

@Entity
@Table(name = "status_notification")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StatusNotificationModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private int id;
	
	@Column(name = "name", length = 100, nullable = false, unique = true)
	private String name;
	
	@Column(name = "description", columnDefinition = "TINYTEXT")
	private String description;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean isDelete = false;
	
	public StatusNotificationModel(int i) {
		this.id = i;
	}
}