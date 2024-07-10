package hcmus.alumni.userservice.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
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
