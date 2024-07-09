package hcmus.alumni.userservice.model.notification;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.io.Serializable;

import hcmus.alumni.userservice.model.UserModel;

@Entity
@Table(name = "notification_change")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationChangeModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, columnDefinition="INT UNSIGNED")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "notification_object_id", nullable = false)
	private NotificationObjectModel notificationObject;
	
	@ManyToOne
	@JoinColumn(name = "actor_id", nullable = false)
	private UserModel actor;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean isDelete = false;
}