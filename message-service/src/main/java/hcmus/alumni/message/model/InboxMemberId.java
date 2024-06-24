package hcmus.alumni.message.model;

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
public class InboxMemberId {
    @Column(name = "inbox_id", nullable = false)
    private Long inboxId;
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InboxMemberId that = (InboxMemberId) o;
        return inboxId.equals(that.inboxId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inboxId, userId);
    }
}
