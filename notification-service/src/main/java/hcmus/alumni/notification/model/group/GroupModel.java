package hcmus.alumni.notification.model.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "`group`")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public enum Privacy {
	 PUBLIC, PRIVATE
	}
	
	@Id
	@Column(name = "id", length = 36, nullable = false)
	private String id;
	
	@Column(name = "name", length = 255, nullable = false)
	private String name;
	
	@Column(name = "creator", length = 36, nullable = false)
	private String creator;
	
	@Column(name = "description", columnDefinition = "TINYTEXT")
	private String description;
	
	@Column(name = "type", length = 50)
	private String type;
	
	@Column(name = "cover_url", columnDefinition = "TINYTEXT")
	private String coverUrl;
	
	@Column(name = "website", columnDefinition = "TINYTEXT")
	private String website;
	
	@Column(name = "privacy", columnDefinition = "ENUM('PUBLIC', 'PRIVATE') DEFAULT 'PUBLIC'")
	@Enumerated(EnumType.STRING)
	private Privacy privacy;
	
	@CreationTimestamp
	@Column(name = "create_at")
	private Date createAt;
	
	@UpdateTimestamp
	@Column(name = "update_at")
	private Date updateAt;
	
	@Column(name = "status_id")
	private Integer status;
	
	@Column(name = "participant_count")
	private Integer participantCount;
}
