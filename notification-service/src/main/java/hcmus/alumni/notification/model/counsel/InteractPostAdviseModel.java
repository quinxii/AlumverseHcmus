package hcmus.alumni.notification.model.counsel;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interact_post_advise")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class InteractPostAdviseModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@EmbeddedId
	private InteractPostAdviseId id;
	
	@Column(name = "react_id", nullable = false)
	private Integer react;
	
	@UpdateTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT(0)")
	private Boolean isDelete = false;
}
