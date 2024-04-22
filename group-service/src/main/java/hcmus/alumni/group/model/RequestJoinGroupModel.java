package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "request_join_group")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestJoinGroupModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "group_id", length = 36, nullable = false)
    private String groupId;

    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isDelete = false;
}
