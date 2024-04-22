package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "group_member")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "group_id", length = 36, nullable = false)
    private String groupId;

    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "role", columnDefinition = "ENUM('ADMIN', 'MOD', 'MEMBER') NOT NULL")
    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDelete = false;

    public enum Role {
        ADMIN, MOD, MEMBER
    }
}

