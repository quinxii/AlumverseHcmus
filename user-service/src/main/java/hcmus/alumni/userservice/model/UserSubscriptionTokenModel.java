package hcmus.alumni.userservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.EmbeddedId;

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
