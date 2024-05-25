package hcmus.alumni.group.common;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GroupPermissions implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean edit;
    private Boolean delete;
}