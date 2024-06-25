package hcmus.alumni.counsel.model.notification;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.io.Serializable;

import hcmus.alumni.counsel.model.UserModel;

@Entity
@Table(name = "notification")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, columnDefinition="INT UNSIGNED")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "notification_object_id", nullable = false)
	private NotificationObjectModel notificationObject;
	
	@ManyToOne
	@JoinColumn(name = "notifier_id", nullable = false)
	private UserModel notifier;
	
	@ManyToOne
	@JoinColumn(name = "status_id", nullable = false)
	private StatusNotificationModel status = new StatusNotificationModel(1);
}