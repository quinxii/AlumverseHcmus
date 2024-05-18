package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "friend")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendModel implements Serializable {
	@EmbeddedId
	private FriendId id;
	
	@CreationTimestamp
	@Column(name = "create_at", nullable = false, updatable = false)
	private Date createAt;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
	private boolean isDelete;
}
