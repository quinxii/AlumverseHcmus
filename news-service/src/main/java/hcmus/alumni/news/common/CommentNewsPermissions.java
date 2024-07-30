package hcmus.alumni.news.common;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CommentNewsPermissions implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean edit;
    private Boolean delete;
}
