package hcmus.alumni.message.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inbox_read_status")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InboxReadStatusModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private InboxReadStatusId id;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    @UpdateTimestamp
    @Column(name = "update_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updateAt;

    public InboxReadStatusModel(InboxReadStatusId id, Long lastReadMessageId) {
        this.id = id;
        this.lastReadMessageId = lastReadMessageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InboxReadStatusId that = (InboxReadStatusId) o;
        return id.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
