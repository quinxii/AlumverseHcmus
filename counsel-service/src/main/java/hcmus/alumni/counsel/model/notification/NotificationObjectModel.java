package hcmus.alumni.counsel.model.notification;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "notification_object")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationObjectModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, columnDefinition="INT UNSIGNED")
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "entity_type", nullable = false)
	private EntityTypeModel entityType;
	
	@Column(name = "entity_id", length = 36, nullable = false)
	private String entityId;
	
	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
	private Boolean isDelete = false;
}