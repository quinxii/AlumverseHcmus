package hcmus.alumni.message.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inbox_member")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InboxMemberModel implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Role {
        ADMIN, MEMBER
    }

    @EmbeddedId
    private InboxMemberId id;

    @MapsId("inboxId")
    @ManyToOne
    @JoinColumn(name = "inbox_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private InboxModel inbox;

    @MapsId("userId")
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private UserModel user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, columnDefinition = "ENUM('ADMIN', 'MEMBER')")
    private Role role = Role.MEMBER;

    @CreationTimestamp
    @Column(name = "joined_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private Date joinedAt;

    @Column(name = "is_delete", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDelete = false;

    public InboxMemberModel(InboxMemberId id) {
        setId(id);
    }

    public void setId(InboxMemberId id) {
        this.id = id;
        if (id.getInboxId() != null) {
            this.inbox = new InboxModel();
            this.inbox.setId(id.getInboxId());
        }
        if (id.getUserId() != null) {
            this.user = new UserModel(id.getUserId());
        }
    }

    public void setInbox(InboxModel inbox) {
        this.inbox = inbox;
        this.id.setInboxId(inbox.getId());
    }

    public void setUser(UserModel user) {
        this.user = user;
        this.id.setUserId(user.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InboxMemberModel that = (InboxMemberModel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
}
