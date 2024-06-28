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
import jakarta.persistence.Embeddable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionTokenId implements Serializable {
	@Column(name = "user_id", length = 36, nullable = false)
	private String userId;
	
	@Column(name = "token", length = 200, nullable = false)
	private String token;
}
