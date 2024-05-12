package hcmus.alumni.event.common;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CommentEventPermissions implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean edit;
    private Boolean delete;
}
