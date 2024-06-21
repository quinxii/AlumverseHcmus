package hcmus.alumni.notification.model.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "request_join_group")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestJoinGroupModel implements Serializable {
	@EmbeddedId
	private GroupUserId id;
	
	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
	private boolean isDelete = false;
	
	public void setIsDelete(boolean isDelete) {
	    this.isDelete = isDelete;
	}
}
