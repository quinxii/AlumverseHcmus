package hcmus.alumni.notification.model.counsel;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class InteractPostAdviseId implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Column(name = "post_advise_id", length = 36, nullable = false)
	private String postAdviseId;
	
	@Column(name = "creator", length = 36, nullable = false)
	private String creator;
}
