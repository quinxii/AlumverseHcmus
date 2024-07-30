package hcmus.alumni.group.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupUserId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "group_id")
    private GroupModel group;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;
}
