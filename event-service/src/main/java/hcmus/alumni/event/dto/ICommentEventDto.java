package hcmus.alumni.event.dto;

import java.util.Date;

public interface ICommentEventDto {
    interface User {
        String getId();
        String getFullName();
        String getAvatarUrl();
    }
    interface Permissions {
        Boolean getEdit();
        Boolean getDelete();
    }

    String getId();
    User getCreator();
    String getParentId();
    String getContent();
    Integer getChildrenCommentNumber();
    Date getCreateAt();
    Date getUpdateAt();
    Permissions getPermissions();
}