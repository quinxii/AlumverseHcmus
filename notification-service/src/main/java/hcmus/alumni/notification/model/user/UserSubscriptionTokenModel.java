package hcmus.alumni.notification.model.user;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EmbeddedId;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[user_subscription_token]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserSubscriptionTokenModel implements Serializable {
	@EmbeddedId
	private UserSubscriptionTokenId id;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;
	
	public UserSubscriptionTokenModel(String userId, String token) {
		this.id = new UserSubscriptionTokenId(userId, token);
	}
	
	public String getUserId() {
		return this.id.getUserId();
	}
	public String getToken() {
		return this.id.getToken();
	}
}
