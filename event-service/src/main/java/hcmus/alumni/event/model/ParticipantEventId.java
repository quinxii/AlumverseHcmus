package hcmus.alumni.event.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEventId implements Serializable {

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "user_id")
    private String userId;
}
