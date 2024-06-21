package hcmus.alumni.notification.model.notification;

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
import jakarta.persistence.Transient;

import java.io.Serializable;

import hcmus.alumni.notification.model.user.UserModel;

import hcmus.alumni.notification.common.NotificationType;

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
	private StatusNotificationModel status;
	
	@Transient
	private UserModel actor;
	
	@Transient
	String notificationImageUrl;
	
	@Transient
	String notificationMessage;
	
	@Transient
	String parentId;
	
	public String getEntityId() {
		return notificationObject.getEntityId();
	}
	
	public String getEntityTable() {
		return notificationObject.getEntityType().getEntityTable();
	}
	
	public NotificationType getNotificationType() {
		return notificationObject.getEntityType().getNotificationType();
	}
	
	public NotificationModel(NotificationModel n, NotificationChangeModel nc) {
		this.id = n.id;
		this.notificationObject = n.notificationObject;
		this.notifier = n.notifier;
		this.status = n.status;
		this.actor = nc.getActor();
	}
	
	public NotificationModel(NotificationModel copy, String notificationImageUrl, String notificationMessage, String parentId) {
        this.id = copy.id;
        this.notificationObject = copy.notificationObject;
        this.notifier = copy.notifier;
        this.status = copy.status;
        this.actor = copy.actor;
        this.notificationImageUrl = notificationImageUrl;
        this.notificationMessage = notificationMessage;
        this.parentId = parentId;
    }
}