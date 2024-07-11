package hcmus.alumni.userservice.model.notification;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import java.io.Serializable;

import hcmus.alumni.userservice.common.NotificationType;

@Entity
@Table(name = "entity_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntityTypeModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, columnDefinition="INT UNSIGNED")
	private Long id;
	
	@Column(name = "entity_table", length = 150, nullable = false)
	private String entityTable;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false)
	private NotificationType notificationType;
	
	@Column(name = "description", length = 100)
	private String description;
}