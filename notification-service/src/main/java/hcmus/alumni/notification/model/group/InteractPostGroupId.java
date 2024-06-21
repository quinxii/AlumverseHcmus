package hcmus.alumni.notification.model.group;

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
public class InteractPostGroupId implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "post_group_id", length = 36, nullable = false)
	private String postGroupId;
	
	@Column(name = "creator", length = 36, nullable = false)
	private String creator;
}
