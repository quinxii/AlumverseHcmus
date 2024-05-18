package hcmus.alumni.group.model;


import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class InteractPostGroupId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "post_group_id", length = 36, nullable = false)
    private String postGroupId;

    @Column(name = "creator", length = 36, nullable = false)
    private String creator;
}
