package hcmus.alumni.notification.model.user;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[user_subscription_token]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserSubscriptionTokenModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id = UUID.randomUUID().toString();
	
	@Column(name = "user_id", length = 36)
	private String userId;
	
	@Column(name = "token", columnDefinition = "TEXT")
	private String token;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;
	
	public UserSubscriptionTokenModel(UserSubscriptionTokenModel copy, String userId) {
		this.userId = userId;
		this.token = copy.token;
	}
}
