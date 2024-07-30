package hcmus.alumni.userservice.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
