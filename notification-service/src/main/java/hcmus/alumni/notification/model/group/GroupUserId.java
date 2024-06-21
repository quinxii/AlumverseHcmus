package hcmus.alumni.notification.model.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserId implements Serializable {
	@Column(name = "group_id", length = 36, nullable = false)
	private String groupId;
	
	@Column(name = "user_id", length = 36, nullable = false)
	private String userId;
}
