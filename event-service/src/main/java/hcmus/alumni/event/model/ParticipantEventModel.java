package hcmus.alumni.event.model;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "[participant_event]")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParticipantEventModel {
	@EmbeddedId
    private ParticipantEventId id;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "create_at")
    private Date createdAt;

    @Column(name = "is_delete")
    private boolean isDelete;
}

