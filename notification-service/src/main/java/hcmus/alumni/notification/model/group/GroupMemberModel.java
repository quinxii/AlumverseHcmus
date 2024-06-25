package hcmus.alumni.notification.model.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "group_member")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberModel implements Serializable {
	@EmbeddedId
	private GroupUserId id;

    @Column(name = "role", columnDefinition = "ENUM('CREATOR', 'ADMIN', 'MEMBER') NOT NULL")
    private String role;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDelete = false;
}
