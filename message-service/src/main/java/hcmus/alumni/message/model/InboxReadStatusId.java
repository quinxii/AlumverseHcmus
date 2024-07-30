package hcmus.alumni.message.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class InboxReadStatusId implements Serializable {
    @Column(name = "inbox_id", nullable = false)
    private Long inboxId;
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    public InboxReadStatusId(InboxMemberId id) {
        this.inboxId = id.getInboxId();
        this.userId = id.getUserId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InboxReadStatusId that = (InboxReadStatusId) o;
        return inboxId.equals(that.inboxId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inboxId, userId);
    }
}
